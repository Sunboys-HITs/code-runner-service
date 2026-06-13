package ru.mkenopsia.coderunnerservice.pool;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public abstract class DockerContainerPool {

    private final Set<String> allContainerIds = ConcurrentHashMap.newKeySet();
    private final DockerClient dockerClient;
    private final BlockingQueue<String> availableContainers;
    private final int poolSize;
    private final String imageName;

    protected abstract String getDockerfileName();

    public abstract String getSourceFileName();

    protected abstract List<String> getCompileCommand(String sourceFileName);

    protected abstract List<String> getRunCommand(String entryPoint);

    public DockerContainerPool(int poolSize, String imageName) {
        this.poolSize = poolSize;
        this.imageName = imageName;
        this.availableContainers = new LinkedBlockingQueue<>();

        var dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        this.dockerClient = DockerClientBuilder.getInstance(dockerClientConfig)
                .withDockerHttpClient(httpClient)
                .build();

        initPool();
    }

    private void initPool() {
        var images = dockerClient.listImagesCmd()
                .withReferenceFilter(imageName)
                .exec();

        if (images.isEmpty()) {
            String projectRoot = System.getProperty("user.dir");
            File dockerfile = new File(projectRoot, getDockerfileName());

            if (!dockerfile.exists()) {
                throw new IllegalStateException("Dockerfile not found: " + dockerfile.getAbsolutePath());
            }

            dockerClient.buildImageCmd()
                    .withDockerfile(dockerfile)
                    .withPull(true)
                    .withNoCache(false)
                    .withTags(Set.of(imageName))
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();
        }

        for (int i = 0; i < poolSize; i++) {
            CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMemory(512 * 1024 * 1024L)
                            .withCpuCount(1L)
                            .withNetworkMode("none")
                            .withAutoRemove(false))
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withTty(false)
                    .exec();
            dockerClient.startContainerCmd(container.getId()).exec();
            availableContainers.add(container.getId());
            allContainerIds.add(container.getId());
            log.info("Создан контейнер [{}]: {}", imageName, container.getId());
        }
    }

    public ExecutionResult execCommand(String containerId, String command) throws Exception {
        var execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        dockerClient.execStartCmd(execCreateCmdResponse.getId())
                .withDetach(false)
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame item) {
                        try {
                            if (item.getStreamType() == StreamType.STDOUT) {
                                stdout.write(item.getPayload());
                            } else if (item.getStreamType() == StreamType.STDERR) {
                                stderr.write(item.getPayload());
                            }
                        } catch (IOException ex) {
                            log.warn("Error reading frame", ex);
                        }
                    }
                })
                .awaitCompletion(30, TimeUnit.SECONDS);

        return new ExecutionResult(stdout.toString(), stderr.toString());
    }

    public void writeFile(String containerId, String content, String filePath) throws Exception {
        String escaped = content.replace("\\", "\\\\").replace("'", "'\\''");
        String cmd = "printf '%s' '" + escaped + "' > " + filePath;
        execCommand(containerId, cmd);
    }

    public String borrowContainer() throws InterruptedException {
        String containerId = availableContainers.poll(30, TimeUnit.SECONDS);
        if (containerId == null) {
            throw new RuntimeException("Нет свободных контейнеров в пуле");
        }
        return containerId;
    }

    public void returnContainer(String containerId) {
        availableContainers.offer(containerId);
    }

    @PreDestroy
    public void close() {
        if (availableContainers.isEmpty() && allContainerIds.isEmpty() && dockerClient != null) {
            try {
                dockerClient.close();
            } catch (IOException e) {
                log.warn("Failed to close Docker client", e);
            }
            return;
        }

        for (String id : allContainerIds) {
            try {
                dockerClient.stopContainerCmd(id).withTimeout(5).exec();
            } catch (Exception e) {
                log.warn("Failed to stop container {}", id, e);
            }
            try {
                dockerClient.removeContainerCmd(id).exec();
            } catch (Exception e) {
                log.warn("Failed to remove container {}", id, e);
            }
        }
        availableContainers.clear();
        allContainerIds.clear();

        try {
            dockerClient.close();
        } catch (IOException e) {
            log.warn("Failed to close Docker client", e);
        }
    }
}
