package ru.mkenopsia.coderunnerservice.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;
import ru.mkenopsia.coderunnerservice.pool.DockerContainerPool;
import ru.mkenopsia.coderunnerservice.pool.ExecutionResult;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DockerCodeExecutionService implements CodeExecutionService {

    private final Map<String, DockerContainerPool> containers;
    private final MeterRegistry meterRegistry;

    public DockerCodeExecutionService(ApplicationContext ctx, MeterRegistry meterRegistry) {
        this.containers = new HashMap<>();
        this.meterRegistry = meterRegistry;
        var beans = ctx.getBeansOfType(DockerContainerPool.class);
        for (var beanName : beans.keySet()) {
            this.containers.put(extractLanguageName(beanName), beans.get(beanName));
        }
    }

    private String extractLanguageName(String beanName) {
        return beanName.replace("DockerContainerPool", "").toLowerCase();
    }

    @Override
    public void execute(CodeExecutionRequest request) {
        try {
            var res = executeCode(request.code(), request.language(), "");
            log.info("Выполнен код с результатом: {}", res);
        } catch (Exception ex) {
            log.error("Ошибка при запуске кода в контейнере", ex);
        }
    }

    @Override
    public String executeCode(String code, String language, String inputData) {
        var pool = containers.get(language);
        if (pool == null) {
            throw new IllegalArgumentException("Не найден пул для языка: " + language);
        }

        meterRegistry.counter("executions.by.language", "language", language).increment();

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return runInContainer(pool, code, inputData);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка выполнения кода", e);
        } finally {
            sample.stop(meterRegistry.timer("executions.duration", "language", language));
        }
    }

    private String runInContainer(DockerContainerPool pool, String code, String inputData) throws Exception {
        String containerId = pool.borrowContainer();
        try {
            pool.writeFile(containerId, code, "/sandbox/" + pool.getSourceFileName());
            pool.writeFile(containerId, inputData, "/sandbox/input.txt");

            ExecutionResult result = pool.execCommand(containerId, "/run.sh");
            return classifyResult(result);
        } finally {
            pool.returnContainer(containerId);
        }
    }

    public String classifyResult(ExecutionResult result) {
        String stderr = result.stderr();
        String stdout = result.stdout();

        if (stderr.contains("error:") ||
                stderr.contains("cannot find symbol") ||
                stderr.contains("expected") ||
                stderr.contains("undefined reference") ||
                stderr.contains("build FAILED")) {
            return "COMPILATION ERROR:\n" + stderr;
        }

        if (!stderr.isEmpty() && stdout.isEmpty()) {
            return "RUNTIME ERROR:\n" + stderr;
        }

        return stdout + (!stderr.isEmpty() ? "\n[WARN] " + stderr : "");
    }
}
