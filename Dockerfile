# Estágio 1: Build (Compilação do código)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copia o ficheiro de dependências e baixa o que for necessário
COPY pom.xml .
RUN mvn dependency:go-offline
# Copia o código fonte e gera o ficheiro .jar
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Run (Execução do microsserviço)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copia apenas o .jar gerado no passo anterior
COPY --from=build /app/target/*.jar app.jar

# Define o comando que vai rodar quando o container subir
ENTRYPOINT ["java", "-jar", "app.jar"]