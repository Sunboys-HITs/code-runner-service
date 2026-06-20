FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw -B -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar
COPY CppDockerfile CSharpDockerfile GoDockerfile JavaDockerfile PythonDockerfile /app/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
