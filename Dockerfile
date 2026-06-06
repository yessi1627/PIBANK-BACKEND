# ============================================================
# Dockerfile — PiBank Backend


# Etapa 1: Construcción con Maven
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar archivos de dependencias primero
COPY pom.xml .
COPY .mvn .mvn
RUN mvn dependency:go-offline -B 2>/dev/null || true

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Imagen de producción mínima
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuario no-root por seguridad (PCI-DSS)
RUN addgroup -g 1001 pibank && \
    adduser -u 1001 -G pibank -s /bin/sh -D pibank

# Copiar JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Crear directorio de logs
RUN mkdir -p /app/logs && chown -R pibank:pibank /app

USER pibank

# Variables de entorno con valores por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
