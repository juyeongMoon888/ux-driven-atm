package com.mybank.atmweb.network;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

public class ServerDownTest {

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    @Tag("network")
    void 서버가_꺼지면_Connection_Refused_예외() {
        Exception exception = assertThrows(Exception.class, () -> {
            restTemplate.getForObject("http://localhost:8080/api/users", String.class);
        });

        String message = exception.getMessage();

        assertTrue(message.contains("Connection refused") || message.contains("ConnectException"));
    }
}
