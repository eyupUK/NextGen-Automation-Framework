# Dockerfile
FROM --platform=linux/amd64 eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Install Maven (package) to provide `mvn` for the build
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy only maven files first for layer caching
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw

# Ensure mvnw is executable (prevents permission/CRLF problems)
RUN chmod +x mvnw

# Download dependencies using the project wrapper
RUN ./mvnw -B -q dependency:go-offline -Dmaven.repo.local=/root/.m2/repository

# Copy source and package (skip tests)
COPY src ./src
COPY pom.xml ./
RUN ./mvnw -B -q -DskipTests package -Dmaven.repo.local=/root/.m2/repository

# Runtime stage: use JDK 21 image to run tests
FROM --platform=linux/amd64 eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy built artifacts and maven wrapper to runtime image
COPY --from=build /app/target /app/target
COPY --from=build /app/mvnw /app/mvnw
COPY --from=build /app/.mvn /app/.mvn
RUN chmod +x mvnw

ENV SELENIUM_URL=http://selenium:4444
ENV MAVEN_OPTS="-Dwebdriver.remote.url=${SELENIUM_URL}"

# Default command runs the UI tests
CMD ["sh", "-c", "./mvnw -B -Dwebdriver.remote.url=${SELENIUM_URL} test"]
