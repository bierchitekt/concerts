# ------------------------------------
# STAGE 1: Build-Umgebung (Nutzt das vollständige Maven/JDK-Image)
# ------------------------------------
FROM maven:3.9.5-openjdk-25 AS builder

# Setzt das Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiert die pom.xml und die Quellcodes
# Wichtig: pom.xml zuerst, um das Caching von Abhängigkeiten zu optimieren
COPY pom.xml .
COPY src /app/src

# Führt den Maven Build aus (Verpackt das Artefakt in target/)
# Hier wird auch das 'verify' implizit ausgeführt, wenn die Phase 'package' läuft
RUN mvn clean package -DskipTests

# ------------------------------------
# STAGE 2: Laufzeit-Umgebung (Nutzt nur das JRE-Image)
# ------------------------------------
FROM openjdk:25-jre-slim

# Erstellt einen Non-Root-User für bessere Sicherheit (empfohlen)
RUN adduser --system --group spring
USER spring:spring

# Kopiert das fertige JAR-Artefakt aus der 'builder'-Stage
# 'app-0.0.1-SNAPSHOT.jar' muss durch den tatsächlichen Namen Ihrer JAR-Datei ersetzt werden!
COPY --from=builder /target/concerts-0.0.1-SNAPSHOT.jar /app/app.jar

# Setzt das Arbeitsverzeichnis
WORKDIR /app

# Definiert den Port, den Ihre Spring Boot-Anwendung verwendet
EXPOSE 8080

# Startet die Anwendung
ENTRYPOINT ["java", "-jar", "/app/app.jar"]