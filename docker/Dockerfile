FROM eclipse-temurin:21-jdk AS build

ARG SERVICE_MODULE
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' ./gradlew && chmod +x ./gradlew
RUN ./gradlew :${SERVICE_MODULE}:bootJar -x test

FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_MODULE
WORKDIR /app
COPY --from=build /workspace/${SERVICE_MODULE}/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
