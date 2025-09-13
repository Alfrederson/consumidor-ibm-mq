# ==========================
# Stage 1: Build
# ==========================
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /build

# copy pom.xml and resolve dependencies first (cache friendly)
COPY pom.xml .
RUN mvn dependency:go-offline

# copy source and build
COPY src ./src
RUN mvn package -DskipTests

# ==========================
# Stage 2: Run
# ==========================
FROM eclipse-temurin:21-jre
WORKDIR /app

# copy the built jar from stage 1
COPY --from=build /build/target/quarkus-app/ /app/

EXPOSE 8080

# run Quarkus app
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
