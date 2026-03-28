# Build
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
COPY src ./src
RUN ./mvnw -q -B -DskipTests package

# Run (Cloud Run sets PORT; Spring reads it via application.yml)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/medibridge-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
# Non-root (nobody exists on eclipse-temurin jammy images)
USER nobody
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
