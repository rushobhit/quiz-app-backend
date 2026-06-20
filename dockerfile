# ==========================
# BUILD STAGE
# ==========================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Maven wrapper + pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

# Dependencies cache
RUN ./mvnw dependency:go-offline -B

# Source code
COPY src src

# Build jar
RUN ./mvnw clean package -DskipTests

# ==========================
# RUNTIME STAGE
# ==========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Dserver.port=${PORT}","-jar","app.jar"]