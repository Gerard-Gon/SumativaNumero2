# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/techstore-api-1.0.0.jar app.jar

# Configurar usuario seguro no-root
RUN adduser -D nobodyuser
USER nobodyuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]