# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar apenas pom.xml primeiro (cache de dependências)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar apenas os arquivos necessários do build
COPY --from=build /app/target/quarkus-app/lib/ /app/lib/
COPY --from=build /app/target/quarkus-app/*.jar /app/
COPY --from=build /app/target/quarkus-app/app/ /app/app/
COPY --from=build /app/target/quarkus-app/quarkus/ /app/quarkus/

# Variáveis de ambiente
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expor porta
EXPOSE 8080

# Comando para iniciar
CMD ["java", "-Dquarkus.http.host=0.0.0.0", "-Dquarkus.http.port=${PORT:-8080}", "-jar", "quarkus-run.jar"]