# ── Stage 1: Build ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build files first for layer caching
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Copy all sub-module build files (for dependency resolution caching)
COPY atlaspay-shared-kernel/build.gradle         atlaspay-shared-kernel/
COPY atlaspay-identity/build.gradle              atlaspay-identity/
COPY atlaspay-accounts/build.gradle              atlaspay-accounts/
COPY atlaspay-ledger/build.gradle                atlaspay-ledger/
COPY atlaspay-transfers/build.gradle             atlaspay-transfers/
COPY atlaspay-charges/build.gradle               atlaspay-charges/
COPY atlaspay-subscriptions/build.gradle         atlaspay-subscriptions/
COPY atlaspay-escrow/build.gradle                atlaspay-escrow/
COPY atlaspay-settlement/build.gradle            atlaspay-settlement/
COPY atlaspay-transaction-splits/build.gradle    atlaspay-transaction-splits/
COPY atlaspay-transactions-query/build.gradle    atlaspay-transactions-query/
COPY atlaspay-notifications/build.gradle         atlaspay-notifications/
COPY atlaspay-rate-limiter/build.gradle          atlaspay-rate-limiter/
COPY atlaspay-eventbus/build.gradle              atlaspay-eventbus/
COPY atlaspay-app/build.gradle                   atlaspay-app/

# Download dependencies (cached unless build files change)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet || true

# Copy all source code
COPY . .

# Build the fat jar — skip tests in Docker build (tests run in CI separately)
RUN ./gradlew :atlaspay-app:bootJar --no-daemon -x test

# ── Stage 2: Extract layers for efficient layer caching ───────────────────────
FROM eclipse-temurin:21-jre-jammy AS extractor

WORKDIR /workspace
COPY --from=builder /workspace/atlaspay-app/build/libs/atlaspay.jar atlaspay.jar

# Spring Boot layer extraction for optimal Docker caching
RUN java -Djarmode=layertools -jar atlaspay.jar extract

# ── Stage 3: Final minimal runtime image ──────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy AS runtime

# Security: non-root user
RUN groupadd -r atlaspay && useradd -r -g atlaspay atlaspay

WORKDIR /app

# Copy extracted layers (ordered by change frequency: dependencies rarely change)
COPY --from=extractor /workspace/dependencies/ ./
COPY --from=extractor /workspace/spring-boot-loader/ ./
COPY --from=extractor /workspace/snapshot-dependencies/ ./
COPY --from=extractor /workspace/application/ ./

# OpenTelemetry Java agent for distributed tracing (downloaded at build time)
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.12.0/opentelemetry-javaagent.jar /app/otel-agent.jar

RUN chown -R atlaspay:atlaspay /app
USER atlaspay

EXPOSE 8080
EXPOSE 8081

# Health check using Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom"

ENV OTEL_OPTS="-javaagent:/app/otel-agent.jar \
               -Dotel.service.name=atlaspay \
               -Dotel.exporter.otlp.endpoint=${OTEL_EXPORTER_ENDPOINT:-http://tempo:4317} \
               -Dotel.traces.exporter=${OTEL_EXPORTER:-none}"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $OTEL_OPTS org.springframework.boot.loader.launch.JarLauncher"]
