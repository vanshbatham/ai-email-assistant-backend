# 1. Use a base image that has both Java 21 AND Maven
FROM maven:3.9.6-eclipse-temurin-21

# Set the working directory
WORKDIR /app

# 2. Copy just the pom.xml first
# This caches dependencies, so they don't re-download every time
COPY pom.xml .

# 3. Use 'mvn' (from the image) instead of './mvnw'
RUN mvn dependency:go-offline

# Copy the rest of your source code
COPY src ./src

# 4. Build the app, skipping tests, using 'mvn'
RUN mvn package -DskipTests

# Expose the port
EXPOSE 8080

# 5. Make sure this .jar file name matches your pom.xml <version>
CMD ["java", "-jar", "target/email-writer-sb-0.0.1-SNAPSHOT.jar"]