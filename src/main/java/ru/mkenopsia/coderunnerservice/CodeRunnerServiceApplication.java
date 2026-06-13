package ru.mkenopsia.coderunnerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;
import ru.mkenopsia.coderunnerservice.rabbit.CodeExecutionConsumer;
import ru.mkenopsia.coderunnerservice.service.TestService;

import java.util.Map;
import java.util.UUID;

@SpringBootApplication
public class CodeRunnerServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CodeRunnerServiceApplication.class, args);

        var testService = context.getBean(TestService.class);
        var consumer = context.getBean(CodeExecutionConsumer.class);

        String taskId = "task-multiply-by-2";

        testService.add(taskId, "2", "4");
        testService.add(taskId, "3", "6");
        testService.add(taskId, "5", "10");
        testService.add(taskId, "0", "0");

        var codes = Map.of(
                "java", "import java.util.*;\npublic class Main { public static void main(String[] a) { Scanner s=new Scanner(System.in); System.out.println(s.nextInt()*2); } }",
                "python", "print(int(input()) * 2)",
                "cpp", "#include <iostream>\nint main() { int x; std::cin >> x; std::cout << x*2; return 0; }",
                "csharp", "using System; class Program { static void Main() { Console.WriteLine(int.Parse(Console.ReadLine()) * 2); } }",
                "go", "package main\nimport \"fmt\"\nfunc main() { var x int; fmt.Scan(&x); fmt.Print(x*2) }"
        );

        for (var entry : codes.entrySet()) {
            System.out.println("\n=== " + entry.getKey().toUpperCase() + " ===");

            consumer.listen(new CodeExecutionRequest(
                    entry.getValue(), entry.getKey(), taskId, UUID.randomUUID()
            ));

            System.out.println();
        }
    }

}
