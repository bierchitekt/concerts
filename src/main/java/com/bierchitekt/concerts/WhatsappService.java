package com.bierchitekt.concerts;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Service
public class WhatsappService {

    @Value("${whatsapp.apikey}")
    @NotEmpty
    private String apikey;

    @Value("${whatsapp.apiurl}")
    @NotEmpty
    private String url;

    public void sendMessage(String channel, String text) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        RestClient restClient = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl(url)
                .build();
        WhatsappMessage whatsappMessage = new WhatsappMessage(
                channel, text, null, false, false, "default");
        restClient.post()

                .uri("/api/sendText")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Api-Key", apikey)
                .body(whatsappMessage)
                .retrieve().toBodilessEntity();
    }
}
