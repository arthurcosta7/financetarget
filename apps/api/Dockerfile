# syntax=docker/dockerfile:1.8
FROM eclipse-temurin:25.0.3_9-jdk-alpine@sha256:5ecfde8e5ecde5954ea3721155b345ef56c1d579b940c761318ad4c05959a151 AS build

ARG VCS_REF
WORKDIR /workspace
COPY apps/api/.mvn .mvn
COPY apps/api/mvnw apps/api/pom.xml ./
COPY apps/api/src src
RUN --mount=type=cache,target=/root/.m2 chmod +x mvnw \
    && ./mvnw --batch-mode -DskipTests -Dapp.release.id="${VCS_REF}" package

FROM eclipse-temurin:25.0.3_9-jre-alpine@sha256:28db6fdf60e38945e43d840c0333aeaec66c15943070104f7586fd3c9d1665b0 AS runtime

ARG VCS_REF
LABEL org.opencontainers.image.title="FinanceTarget API" \
      org.opencontainers.image.source="https://github.com/arthurcosta7/financetarget" \
      org.opencontainers.image.revision="${VCS_REF}"

RUN addgroup -S -g 10001 financetarget \
    && adduser -S -D -H -u 10001 -G financetarget financetarget
WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/api-0.1.0-SNAPSHOT.jar app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/urandom"
EXPOSE 8080
USER 10001:10001
HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=5 \
  CMD wget -q -O - http://127.0.0.1:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
