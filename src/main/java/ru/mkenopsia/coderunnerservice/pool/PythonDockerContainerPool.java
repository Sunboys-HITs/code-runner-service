package ru.mkenopsia.coderunnerservice.pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PythonDockerContainerPool extends DockerContainerPool {

    public PythonDockerContainerPool(
            @Value("${docker-containers.pool.python.size:2}") int poolSize,
            @Value("${docker-containers.pool.python.image-name:}") String imageName) {
        super(poolSize, imageName);
    }

    @Override
    protected String getDockerfileName() {
        return "PythonDockerfile";
    }

    @Override
    public String getSourceFileName() {
        return "main.py";
    }

    @Override
    protected List<String> getCompileCommand(String sourceFileName) {
        return null;
    }

    @Override
    protected List<String> getRunCommand(String entryPoint) {
        return List.of("python3", "-u", entryPoint);
    }
}
