# One image serving both the Angular UI and the Spring Boot API.

# 1) Build the Angular app
FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build --configuration production

# 2) Build the Spring Boot jar with the UI bundled as static resources
FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /app/dist/frontend/browser ./src/main/resources/static
# Tests run in CI / locally (mvn test); skipped here to keep image builds fast.
RUN mvn -q -B -DskipTests package

# 3) Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /data
COPY --from=backend /app/target/salary-management-*.jar app.jar
ENV APP_DB_PATH=/data/salary.db \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
VOLUME /data
EXPOSE 8080
# On first start the database is empty, so the seeder inserts 10,000 employees automatically.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
