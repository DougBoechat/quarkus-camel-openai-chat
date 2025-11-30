# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar apenas pom.xml primeiro (cache de dependências)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests -B

# Verificar estrutura do build (debug)
RUN ls -la target/quarkus-app/

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar TODA a pasta quarkus-app de uma vez
COPY --from=build /app/target/quarkus-app/ /app/

# Variáveis de ambiente
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expor porta
EXPOSE 8080

# Comando para iniciar
CMD ["sh", "-c", "java $JAVA_OPTS -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT:-8080} -jar quarkus-run.jar"]