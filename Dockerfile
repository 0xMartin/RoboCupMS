# --- Build Stage ---
# Use a full JDK to build the application
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy build files first to leverage Docker's layer cache
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Make the wrapper executable
RUN chmod +x ./gradlew

# Download dependencies. This layer is cached and only re-run when build.gradle changes.
RUN ./gradlew dependencies

# Copy the rest of the source code
COPY src ./src

# Build the application JAR
RUN ./gradlew build -x test


# --- Final Stage ---
# Use a smaller JRE image for the final artifact
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy the SSL certificate needed for runtime
COPY robocupms.p12 .
COPY config.json .

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
