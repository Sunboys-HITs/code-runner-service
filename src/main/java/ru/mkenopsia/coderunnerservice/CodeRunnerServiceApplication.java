package ru.mkenopsia.coderunnerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;
import ru.mkenopsia.coderunnerservice.service.DockerCodeExecutionService;

import java.util.Map;
import java.util.UUID;

@SpringBootApplication
public class CodeRunnerServiceApplication {

    public static void main(String[] args) throws InterruptedException {
        var context = SpringApplication.run(CodeRunnerServiceApplication.class, args);

//        Thread.sleep(5_000);
//
        var service = (DockerCodeExecutionService) context.getBean("dockerCodeExecutionService");
//
//        String javaCode =
//                "import java.util.Scanner;\n" +
//                        "public class Main {\n" +
//                        "    public static void main(String[] args) {\n" +
//                        "        Scanner s = new Scanner(System.in);\n" +
//                        "        int x = s.nextInt();\n" +
//                        "        System.out.println(x * x);\n" +
//                        "    }\n" +
//                        "}";
//
//        service.execute(new CodeExecutionRequest(
//                javaCode, "java", "1", "123", UUID.randomUUID()
//        ));

        var tests = Map.of(
                "java", "import java.util.*;\npublic class Main { public static void main(String[] a) { Scanner s=new Scanner(System.in); System.out.println(s.nextInt()*2); } }",
                "python", "print(int(input()) * 2)",
                "cpp", "#include <iostream>\nint main() { int x; std::cin >> x; std::cout << x*2; return 0; }",
                "csharp", "using System; class Program { static void Main() { Console.WriteLine(int.Parse(Console.ReadLine()) * 2); } }",
                "go", "package main\nimport \"fmt\"\nfunc main() { var x int; fmt.Scan(&x); fmt.Print(x*2) }"
        );

        for (var entry : tests.entrySet()) {
            service.execute(new CodeExecutionRequest(entry.getValue(), entry.getKey(), "21", "21", UUID.randomUUID()));
        }
    }

}
