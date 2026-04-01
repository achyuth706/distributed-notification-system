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
COPY email-service/pom.xml ./email-service/pom.xml
COPY email-service/src ./email-service/src
RUN cd email-service && mvn package -q -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=app-build /workspace/email-service/target/*.jar app.jar
USER appuser
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
