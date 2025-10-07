FROM maven:3.9.6-amazoncorretto-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine

# Install wget for health checks
RUN apk add --no-cache wget

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV DISCORD_TOKEN=""
ENV DISCORD_GUILD_ID=""
ENV MCP_BEARER_TOKEN=""

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
