package ru.mkenopsia.coderunnerservice.pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CSharpDockerContainerPool extends DockerContainerPool {

    public CSharpDockerContainerPool(
            @Value("${docker-containers.pool.csharp.size:2}") int poolSize,
            @Value("${docker-containers.pool.csharp.image-name:csharp-runner:latest}") String imageName) {
        super(poolSize, imageName);
    }

    @Override
    protected String getDockerfileName() {
        return "CSharpDockerfile";
    }

    @Override
    public String getSourceFileName() {
        return "main.cs";
    }

    @Override
    protected List<String> getCompileCommand(String sourceFileName) {
        return List.of("dotnet", "build", "-c", "Release", "-o", "/sandbox/out");
    }

    @Override
    protected List<String> getRunCommand(String entryPoint) {
        return List.of("dotnet", "/sandbox/out/" + entryPoint);
    }
}
