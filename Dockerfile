FROM gradle:9.5.1-jdk25 AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test


FROM eclipse-temurin:25-jdk-noble
USER root
RUN apt-get update \
  && apt-get install -y --no-install-recommends default-mysql-client \
  && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
