package ru.mkenopsia.coderunnerservice.pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoDockerContainerPool extends DockerContainerPool {

    public GoDockerContainerPool(
            @Value("${docker-containers.pool.go.size:2}") int poolSize,
            @Value("${docker-containers.pool.go.image-name:go-runner:latest}") String imageName) {
        super(poolSize, imageName);
    }

    @Override
    protected String getDockerfileName() {
        return "GoDockerfile";
    }

    @Override
    public String getSourceFileName() {
        return "main.go";
    }

    @Override
    protected List<String> getCompileCommand(String sourceFileName) {
        return List.of("go", "build", "-o", "main", sourceFileName);
    }

    @Override
    protected List<String> getRunCommand(String entryPoint) {
        return List.of("./" + entryPoint);
    }
}
