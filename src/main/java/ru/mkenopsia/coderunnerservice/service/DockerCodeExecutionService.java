package ru.mkenopsia.coderunnerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;
import ru.mkenopsia.coderunnerservice.service.pool.ExecutionResult;
import ru.mkenopsia.coderunnerservice.service.pool.JavaDockerContainerPool;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerCodeExecutionService implements CodeExecutionService {

    private final JavaDockerContainerPool javaDockerContainerPool;

    @Override
    public void execute(CodeExecutionRequest request) {
        if("java".equals(request.language())) {
            try {
                var res = executeJavaCode(request.code(), request.tests());
                log.info("Выполнен код с результатом: {}", res);
            } catch (Exception ex) {
                log.error("Ошибка при запуске кода в контейнере", ex);
            }
        }
    }

    public String executeJavaCode(String javaCode, String inputData) throws Exception {
        String containerId = javaDockerContainerPool.borrowContainer();
        try {
            String escapedCode = javaCode.replace("'", "'\\''");
            String createJavaCmd = "bash -c \"cat > /sandbox/Main.java\" <<< '" + escapedCode + "'";
            javaDockerContainerPool.execCommand(containerId, createJavaCmd);

            String escapedInput = inputData.replace("'", "'\\''");
            String createInputCmd = "bash -c \"cat > /sandbox/input.txt\" <<< '" + escapedInput + "'";
            javaDockerContainerPool.execCommand(containerId, createInputCmd);

            ExecutionResult result = javaDockerContainerPool.execCommand(containerId, "/run.sh");

            if (!result.stderr().isEmpty() && result.stdout().isEmpty()) {
                return "COMPILATION ERROR:\n" + result.stderr();
            }
            return result.stdout();
        } finally {
            // Чистка
            javaDockerContainerPool.execCommand(
                    containerId,
                    "rm -f /sandbox/Main.java /sandbox/Main.class /sandbox/input.txt"
            );
            javaDockerContainerPool.returnContainer(containerId);
        }
    }
}
