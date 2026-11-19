FROM maven:3.9.9-eclipse-temurin-17 AS maven

FROM debian:bookworm-slim
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        default-jdk-headless chromium chromium-driver ca-certificates \
    && rm -rf /var/lib/apt/lists/*
COPY --from=maven /usr/share/maven /usr/share/maven
ENV JAVA_HOME=/usr/lib/jvm/default-java \
    MAVEN_HOME=/usr/share/maven \
    PATH=/usr/share/maven/bin:${PATH} \
    CHROME_BIN=/usr/bin/chromium \
    CHROME_NO_SANDBOX=true \
    MAVEN_OPTS="-Dwebdriver.chrome.driver=/usr/bin/chromedriver"
WORKDIR /tests
COPY pom.xml ./
RUN mvn -B --no-transfer-progress org.apache.maven.plugins:maven-dependency-plugin:3.8.1:go-offline
COPY src ./src
RUN mvn -B --no-transfer-progress test-compile
ENTRYPOINT ["mvn", "-B", "--no-transfer-progress", "test", "-Dwebdriver.chrome.driver=/usr/bin/chromedriver"]
