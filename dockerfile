# === Stage 1: build ===
FROM gradle:7.6.1-jdk17 AS builder
WORKDIR /app

# Copia todo o projeto e roda o build do fatJar
COPY --chown=gradle:gradle . .
RUN gradle fatJar --no-daemon

# === Stage 2: runtime ===
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copia o fat.jar gerado no builder
COPY --from=builder /app/build/libs/fat.jar ./fat.jar

# Expõe a porta e repassa a variável de ambiente PORT
EXPOSE 8080
ENV PORT=${PORT}

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "fat.jar"]
