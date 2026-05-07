package ru.mkenopsia.coderunnerservice.service.pool;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class DockerContainerPool {
    private final DockerClient dockerClient;
    private final BlockingQueue<String> availableContainers;
    private final int poolSize;
    private final String imageName;

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
        dockerClient.buildImageCmd()
                .withDockerfile(new File("./JavaDockerfile"))
                .withPull(true)
                .withNoCache(true)
                .withTags(Set.of(imageName))
                .exec(new BuildImageResultCallback())
                .awaitImageId();

        for (int i = 0; i < poolSize; i++) {
            CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMemory(512 * 1024 * 1024L)
                            .withCpuCount(1L)
                            .withAutoRemove(false))
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withTty(false)
                    .exec();
            dockerClient.startContainerCmd(container.getId()).exec();
            availableContainers.add(container.getId());
            System.out.println("Создан контейнер: " + container.getId());
        }
    }

    public String borrowContainer() throws InterruptedException {
        String containerId = availableContainers.poll(10, TimeUnit.SECONDS);
        if (containerId == null) {
            throw new RuntimeException("Нет свободных контейнеров в пуле");
        }
        return containerId;
    }

    public void returnContainer(String containerId) {
        // TODO: очистить /sandbox внутри контейнера
        // Пока просто возвращаем
        availableContainers.offer(containerId);
    }

    public ExecutionResult execCommand(String containerId, String command) throws Exception {
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("bash", "-c", command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        dockerClient.execStartCmd(execCreateCmdResponse.getId())
                .withDetach(false)
//                .exec(new ExecStartResultCallback(stdout, stderr))
                .exec(new ResultCallback.Adapter<>() {
                    @Override
                    public void onNext(Frame item) {
                        try {
                            if (item.getStreamType() == StreamType.STDOUT) {
                                stdout.write(item.getPayload());
                            } else if (item.getStreamType() == StreamType.STDERR) {
                                stderr.write(item.getPayload());
                            }
                        } catch (IOException ex) {
                            // todo
                        }
                    }
                })
                .awaitCompletion(5, TimeUnit.SECONDS);

        return new ExecutionResult(stdout.toString(), stderr.toString());
    }

    private void execInContainer(String containerId, String command) throws Exception {
        com.github.dockerjava.api.command.ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerId)
                .withCmd("bash", "-c", command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();
        dockerClient.execStartCmd(exec.getId()).exec(new ResultCallback.Adapter<>()).awaitCompletion();
    }

    public void close() throws IOException {
        for (String id : availableContainers) {
            dockerClient.stopContainerCmd(id).exec();
            dockerClient.removeContainerCmd(id).exec();
        }
        dockerClient.close();
    }
}
