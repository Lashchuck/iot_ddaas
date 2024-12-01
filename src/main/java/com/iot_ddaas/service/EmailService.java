package com.iot_ddaas.service;

import jakarta.mail.IllegalWriteException;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Klasa usługi do wysyłania powiadomień e-mail, w przypadku anomalii.
 * Używa API Sendinblue do wysyłania wiadomości e-mail.
 */
@Service
public class EmailService {

    // Klucz API dla usługi Sendinblue
    @Value("${sendinblue.api.key}")
    private String apiKey;

    // RestTemplate do wysyłania żądań HTTP
    private final RestTemplate restTemplate = new RestTemplate();

    // Mapowanie userId i deviceId na adres email
    private Map<Long, String> userEmailMap;
    private Map<String, String> deviceEmailMap;

    /**
     * Konstruktor inicjalizujący mapę e-mail użytkownika.
     * Mapa służy do mapowania identyfikatorów użytkowników na odpowiadające im adresy e-mail.
     */
    public EmailService() {
        userEmailMap = new HashMap<>();
        userEmailMap.put(1L, "matiif380@gmail.com");
        userEmailMap.put(2L, "mateusz.laszczak19@gmail.com");
    }

    /**
     * Wysyłanie powiadomienń e-mail o anomalii do użytkownika zidentyfikowanego przez userId lub deviceId.
     * Jeśli brakuje obu lub są nieprawidłowe, zgłaszany jest wyjątek.
     */
    public void sendAnomalyNotification(Long userId, String deviceId, String message) throws MessagingException{
        String userEmail = null;

        // Znalezienie email na podstawie userId
        if (userId !=null){
            userEmail = userEmailMap.get(userId);
        }

        // Jeżeli nie znaleziono email na podstawie userId, szukanie na podstawie deviceId
        if (userEmail == null && deviceId !=null){
            userEmail = deviceEmailMap.get(deviceId);
        }

        // Jeżeli nie ma emaila, zgłaszanie błędu
        if (userEmail == null){
            throw new IllegalWriteException("Email address not found for userId: " + userId + " neither deviceId: " + deviceId);
        }

        // Tworzenie wiadomości email
        String url = "https://api.sendinblue.com/v3/smtp/email";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey); // // Ustawienie klucza API Sendinblue

        // Konstruowanie pliku JSON wiadomości e-mail
        String json = "{"
                + "\"sender\": {\"name\":\"IoT Notifier\", \"email\":\"matiif380@gmail.com\"},"
                + "\"to\": [{\"email\":\"" + userEmail + "\"}],"
                + "\"subject\":\"Anomaly notification\","
                + "\"htmlContent\":\"" + message + "\""
                + "}";

        // Utworzenie encji żądania HTTP z plikiem JSON i nagłówkami
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        try {
            // Wysyłanie żądania HTTP POST do interfejsu API Sendinblue
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            System.out.println("Sendinblue response: " + response.getBody());
        } catch (Exception e) {
            System.out.println("Error sending email via Sendinblue: " + e.getMessage());
            throw  new MessagingException("Failed to send email", e);
        }
    }
}
