# Etapa 1: construir el jar con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn -B dependency:go-offline
COPY src/ src/
RUN mvn -B clean package -DskipTests

# Etapa 2: extraer capas del jar (layertools)
FROM eclipse-temurin:21-jre AS layers
WORKDIR /build
COPY --from=build /build/target/biblioteca-spring-*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Etapa 3: imagen final, usuario sin privilegios
FROM eclipse-temurin:21-jre
RUN groupadd --system biblioteca \
    && useradd --system --gid biblioteca biblioteca
WORKDIR /app
COPY --from=layers /build/dependencies/ ./
COPY --from=layers /build/spring-boot-loader/ ./
COPY --from=layers /build/snapshot-dependencies/ ./
COPY --from=layers /build/application/ ./
USER biblioteca

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
