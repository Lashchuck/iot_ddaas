package com.iot_ddaas.service;

import jakarta.mail.IllegalWriteException;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${sendinblue.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Mapowanie userId i deviceId na adres email
    private Map<Long, String> userEmailMap;
    private Map<String, String> deviceEmailMap;

    public EmailService() {

        userEmailMap = new HashMap<>();
        userEmailMap.put(1L, "matiif380@gmail.com");
        userEmailMap.put(2L, "mateusz.laszczak19@gmail.com");
    }

    // Wysyłanie powiadomienia na podstawie userId lub deviceId
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
            throw new IllegalWriteException("Nie znaleziono adresu email dla userId: " + userId + " ani deviceId: " + deviceId);
        }

        // Tworzenie wiadomości email
        String url = "https://api.sendinblue.com/v3/smtp/email";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        String json = "{"
                + "\"sender\": {\"name\":\"IoT Notifier\", \"email\":\"matiif380@gmail.com\"},"
                + "\"to\": [{\"email\":\"" + userEmail + "\"}],"
                + "\"subject\":\"Anomaly notification\","
                + "\"htmlContent\":\"" + message + "\""
                + "}";

        HttpEntity<String> request = new HttpEntity<>(json, headers);

        restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }
}
