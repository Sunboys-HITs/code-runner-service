package ru.mkenopsia.coderunnerservice.pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CppDockerContainerPool extends DockerContainerPool {

    public CppDockerContainerPool(
            @Value("${docker-containers.pool.cpp.size:2}") int poolSize,
            @Value("${docker-containers.pool.cpp.image-name:cpp-runner:latest}") String imageName) {
        super(poolSize, imageName);
    }

    @Override
    protected String getDockerfileName() {
        return "CppDockerfile";
    }

    @Override
    public String getSourceFileName() {
        return "main.cpp";
    }

    @Override
    protected List<String> getCompileCommand(String sourceFileName) {
        return List.of("g++", "-std=c++17", "-O2", "-Wall", "-Wextra",
                "-Werror", "-fsanitize=address", "-o", "Main", sourceFileName);
    }

    @Override
    protected List<String> getRunCommand(String entryPoint) {
        return List.of("./" + entryPoint);
    }
}
