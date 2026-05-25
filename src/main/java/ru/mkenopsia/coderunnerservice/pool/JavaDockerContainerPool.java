package ru.mkenopsia.coderunnerservice.pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JavaDockerContainerPool extends DockerContainerPool {

    public JavaDockerContainerPool(
            @Value("${docker-containers.pool.java.size}") int poolSize,
            @Value("${docker-containers.pool.java.image-name}") String imageName) {
        super(poolSize, imageName);
    }

    @Override
    protected String getDockerfileName() {
        return "JavaDockerfile";
    }

    @Override
    public String getSourceFileName() {
        return "Main.java";
    }

    @Override
    protected List<String> getCompileCommand(String sourceFileName) {
        return List.of("sh", "-c", "javac Main.java 2> compile_err.log");
    }

    @Override
    protected List<String> getRunCommand(String entryPoint) {
        return List.of("sh", "-c", "java Main < input.txt");
    }
}
