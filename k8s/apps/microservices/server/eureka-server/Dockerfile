# Dockerfile multi-etapa optimizado para eureka-server
# =========================================
#
# Mejoras implementadas:
# 1. Dockerfiles multi-etapa optimizados:
#    - Separación clara entre etapa de construcción y ejecución
#    - Uso de imágenes Alpine para la construcción (más ligeras)
#    - Uso de imágenes Distroless para la ejecución (máxima seguridad)
#    - Extracción del JAR en capas para optimizar la caché de Docker
# 2. Optimización de capas:
#    - Las dependencias se descargan solo cuando cambia el pom.xml
#    - El código fuente se compila solo cuando cambia el código
#    - Las capas de la aplicación se organizan por frecuencia de cambio

# Etapa de construcción
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar archivos de Maven y descargar dependencias (aprovecha la caché)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# Compilar la aplicación
COPY src src
RUN ./mvnw package -DskipTests

# Extraer el JAR para optimizar capas
RUN mkdir -p target/extracted
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# Etapa final con imagen mínima
FROM gcr.io/distroless/java21-debian12
WORKDIR /app

# Copiar capas de la aplicación por orden de cambio (optimiza la caché)
COPY --from=builder /app/target/extracted/dependencies/ ./
COPY --from=builder /app/target/extracted/spring-boot-loader/ ./
COPY --from=builder /app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/target/extracted/application/ ./

# Configuración de la aplicación
ENV TZ=America/Caracas

# Puerto y punto de entrada
EXPOSE 8761
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
