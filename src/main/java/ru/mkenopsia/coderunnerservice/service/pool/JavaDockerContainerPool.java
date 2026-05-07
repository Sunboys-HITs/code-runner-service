package ru.mkenopsia.coderunnerservice.service.pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JavaDockerContainerPool extends DockerContainerPool{

    private final int poolSize;
    private final String imageName;

    public JavaDockerContainerPool(
            @Value("${docker-containers.pool.java.size}") int poolSize,
            @Value("${docker-containers.pool.java.image-name}") String imageName) {
        super(poolSize, imageName);
        this.poolSize = poolSize;
        this.imageName = imageName;
    }
}
