package com.cryptovault.services;

import com.cryptovault.dtos.TimeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TimeSchedulerService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 1000)
    public void fetchAndBroadcastTime() {
        String url = "http://worldtimeapi.org/api/timezone/Europe/Warsaw";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<TimeResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, TimeResponse.class);

            if (response.getBody() != null) {
                System.out.println("Otrzymano czas: " + response.getBody().getDatetime());
                messagingTemplate.convertAndSend("/topic/time", response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Podczas otrzymania czasu doszlo do pomylki: " + e.getMessage());
        }
    }
}