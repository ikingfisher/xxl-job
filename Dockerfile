FROM openjdk:17-jdk-alpine

RUN apk add --no-cache bash

WORKDIR /app

COPY xxl-job-admin/target/xxl-job-admin-3.0.1-SNAPSHOT.jar /app
COPY xxl-job-executor-samples/xxl-job-executor-sample-springboot/target/xxl-job-executor-sample-springboot-3.0.1-SNAPSHOT.jar /app

COPY xxl-job-executor-samples/xxl-job-executor-sample-springboot/target/sample-job-0.0.1-SNAPSHOT.jar /app

COPY xxl-job-executor-samples/xxl-job-executor-sample-springboot/target/simple-job-0.0.1-SNAPSHOT.jar /app

# EXPOSE 8080

# ENTRYPOINT ["java", "-jar", "/app/xxl-job-admin-3.0.1-SNAPSHOT.jar"]