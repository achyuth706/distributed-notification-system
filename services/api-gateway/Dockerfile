# syntax=docker/dockerfile:1
# Build context: ./services (parent of api-gateway)

# Stage 1: Build shared-lib
FROM eclipse-temurin:21-jdk-alpine AS shared-lib-build
WORKDIR /workspace
RUN apk add --no-cache maven
COPY shared-lib/pom.xml ./shared-lib/pom.xml
COPY shared-lib/src ./shared-lib/src
RUN cd shared-lib && mvn install -q -DskipTests

# Stage 2: Build api-gateway
FROM eclipse-temurin:21-jdk-alpine AS app-build
WORKDIR /workspace
RUN apk add --no-cache maven
COPY --from=shared-lib-build /root/.m2 /root/.m2
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY api-gateway/src ./api-gateway/src
RUN cd api-gateway && mvn package -q -DskipTests

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=app-build /workspace/api-gateway/target/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
