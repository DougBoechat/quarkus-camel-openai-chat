# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar projeto
COPY pom.xml .
COPY src ./src

# Build da aplicação (gera uber-jar)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Com uber-jar, o arquivo fica em target/*-runner.jar
COPY --from=build /app/target/*-runner.jar /app/app.jar

# Expor porta
EXPOSE 8080

# Comando para iniciar
CMD ["sh", "-c", "java -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT:-8080} -jar app.jar"]