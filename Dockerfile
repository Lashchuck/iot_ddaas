# Użycie obrazu bazowego z Javy
FROM openjdk:22-jdk-slim

# Ustawienie katalogu roboczego w kontenerze
WORKDIR /app

# Skopiowanie plik JAR do katalogu roboczego
COPY target/demo-0.0.1-SNAPSHOT.jar /app/demo.jar

# Określenie na jakim porcie działa aplikacja
EXPOSE 8080

# Uruchomienie aplikacji
CMD ["java", "-jar", "demo.jar"]