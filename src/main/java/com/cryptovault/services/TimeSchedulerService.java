package com.cryptovault.services;

import com.cryptovault.dtos.TimeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TimeSchedulerService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 5000, initialDelay = 5000)
    public void fetchAndBroadcastTime() {
        String url = "https://timeapi.io/api/Time/current/zone?timeZone=Europe/Warsaw";

        try {
            TimeResponse timeData = restTemplate.getForObject(url, TimeResponse.class);

            if (timeData != null) {
                System.out.println("Otzymano czas: " + timeData.getDateTime());

                messagingTemplate.convertAndSend("/topic/time", timeData);
            }
        } catch (Exception e) {
            System.err.println("Podczas otrzymania czasu doszło do pomyłki: " + e.getMessage());
        }
    }
}