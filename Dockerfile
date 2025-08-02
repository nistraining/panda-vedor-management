FROM openjdk:17-jdk-slim

WORKDIR /app

LABEL maintainer="nisith@example.com"
LABEL service="vendor-management"

# Install netcat for health checks
RUN apt-get update && apt-get install -y netcat

EXPOSE 5000

COPY target/panda-vendor-management-0.0.1-SNAPSHOT.war panda-vendor-management.war
COPY wait-for-kafka.sh wait-for-kafka.sh
RUN chmod +x wait-for-kafka.sh

ENTRYPOINT ["./wait-for-kafka.sh"]