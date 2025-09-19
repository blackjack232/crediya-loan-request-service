# Etapa de construcción
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon -x validateStructure

# Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app

# 👇 Ajusta el nombre del JAR aquí
COPY --from=build /app/applications/app-service/build/libs/api-loan-request.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java","-jar","app.jar"]
