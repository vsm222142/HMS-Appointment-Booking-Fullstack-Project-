# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory for doctor profile images
RUN mkdir -p uploads

EXPOSE 8080

# If DB_HOST is set (Render environment), build the JDBC PostgreSQL URL
# Otherwise the application.properties default (MySQL) will be used
ENTRYPOINT ["sh", "-c", "\
  if [ -n \"$DB_HOST\" ]; then \
    export SPRING_DATASOURCE_URL=\"jdbc:postgresql://$DB_HOST:${DB_PORT:-5432}/${DB_NAME:-hms_db}\"; \
    export DB_DRIVER=\"org.postgresql.Driver\"; \
    export DB_DIALECT=\"org.hibernate.dialect.PostgreSQLDialect\"; \
  fi; \
  exec java -Xmx512m -jar app.jar \
"]
