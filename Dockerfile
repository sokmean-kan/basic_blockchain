FROM eclipse-temurin:17-jre-alpine

# Create a folder for the app
WORKDIR /app

# Copy the jar we just built into the container
# Make sure the path matches where your jar is!
COPY build/libs/app.jar app.jar

# Tell Docker to run the jar when it starts
ENTRYPOINT ["java", "-jar", "app.jar"]