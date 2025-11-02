
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./

# Copy application sources
COPY src ./src

# Build the Spring Boot executable (runs tests by default)
RUN mvn -B clean package

FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /workspace/target/pet-clinic-1.0.0.jar app.jar

EXPOSE 8081

# Launch the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
