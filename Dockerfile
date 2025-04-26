# === Stage 1: build ===
FROM gradle:7.6.1-jdk17 AS builder
WORKDIR /app

# Copia todo o projeto, incluindo gradlew e pasta gradle/
COPY . .

# Torna o wrapper executável
RUN chmod +x ./gradlew

# Roda o fatJar usando o Gradle Wrapper
RUN ./gradlew fatJar --no-daemon

# === Stage 2: runtime ===
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copia o fat.jar gerado no builder
COPY --from=builder /app/build/libs/fat.jar ./fat.jar

# Expõe a porta padrão e usa a variável $PORT injetada pelo ambiente
EXPOSE 8080
ENV PORT=${PORT}

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "fat.jar"]
