# 1) Build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Mejor cache de dependencias
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copia el resto y compila
COPY . .
RUN mvn -q clean package -DskipTests

# 2) Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

# 'exec' para que Java sea PID 1 (mejor señales / shutdown)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
