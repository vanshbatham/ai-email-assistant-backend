# Use the official Java 21 base image (matches your project's Java version)
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml
# This caches our dependencies for faster builds
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# Download all dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of your source code
COPY src ./src

# Build the application, skipping tests
RUN ./mvnw package -DskipTests

# Expose the port your Spring app runs on
EXPOSE 8080

# --- This is the command to run your app ---
#
# IMPORTANT: Check your pom.xml for the <artifactId> and <version>.
# The default name is target/[artifactId]-[version].jar
#
CMD ["java", "-jar", "target/email-writer-sb-0.0.1-SNAPSHOT.jar"]