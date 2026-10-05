# One Dockerfile for all Spring services: docker build --build-arg MODULE=order-service .
FROM maven:3.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /build
COPY pom.xml .
COPY discovery-service/pom.xml discovery-service/
COPY config-service/pom.xml config-service/
COPY gateway-service/pom.xml gateway-service/
COPY inventory-service/pom.xml inventory-service/
COPY order-service/pom.xml order-service/
COPY notification-service/pom.xml notification-service/
COPY ${MODULE}/src ${MODULE}/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -B -pl ${MODULE} -am -DskipTests package \
    && cp ${MODULE}/target/${MODULE}-*.jar /build/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/app.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Xmx256m"
ENTRYPOINT ["java", "-jar", "app.jar"]
