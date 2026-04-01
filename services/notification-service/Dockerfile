FROM eclipse-temurin:21-jdk-alpine AS shared-lib-build
WORKDIR /workspace
RUN apk add --no-cache maven
COPY shared-lib/pom.xml ./shared-lib/pom.xml
COPY shared-lib/src ./shared-lib/src
RUN cd shared-lib && mvn install -q -DskipTests

FROM eclipse-temurin:21-jdk-alpine AS app-build
WORKDIR /workspace
RUN apk add --no-cache maven
COPY --from=shared-lib-build /root/.m2 /root/.m2
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY notification-service/src ./notification-service/src
RUN cd notification-service && mvn package -q -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=app-build /workspace/notification-service/target/*.jar app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
