package ru.mkenopsia.coderunnerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;
import ru.mkenopsia.coderunnerservice.service.DockerCodeExecutionService;

import java.util.UUID;

@SpringBootApplication
public class CodeRunnerServiceApplication {

    public static void main(String[] args) throws InterruptedException {
        var context = SpringApplication.run(CodeRunnerServiceApplication.class, args);

        Thread.sleep(5_000);

        var service = (DockerCodeExecutionService) context.getBean("dockerCodeExecutionService");

        String javaCode =
                "import java.util.Scanner;\n" +
                        "public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner s = new Scanner(System.in);\n" +
                        "        int x = s.nextInt();\n" +
                        "        System.out.println(x * x);\n" +
                        "    }\n" +
                        "}";

        service.execute(new CodeExecutionRequest(
                javaCode, "java", "1", "123", UUID.randomUUID()
        ));
    }

}
