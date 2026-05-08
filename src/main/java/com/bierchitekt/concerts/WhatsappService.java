package com.bierchitekt.concerts;

import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Service
@Slf4j
public class WhatsappService {

    @Value("${whatsapp.apikey}")
    @NotEmpty
    private String apikey;

    @Value("${whatsapp.apiurl}")
    @NotEmpty
    private String url;

    private RestClient getRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl(url)
                .defaultHeader("X-Api-Key", apikey)
                .build();

    }

    public void sendMessage(String channel, String text) {

        int retries = 0;
        while(!workerHasStatus("WORKING") && retries < 10){
            activateWorker();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                log.warn("sleep not successful", e);
            }
            retries++;
        }

        WhatsappMessage whatsappMessage = new WhatsappMessage(
                channel, text, null, false, false, "default");

        getRestClient().post()
                .uri("/api/sendText")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Api-Key", apikey)
                .body(whatsappMessage)
                .retrieve().toBodilessEntity();
    }

    public void activateWorker() {
        if (workerHasStatus("STOPPED")) {
            log.info("whatsapp worker was stopped. Starting now");
            startWorker();
        }
    }

    private void startWorker() {

        getRestClient().post()
                .uri("/api/sessions/default/start")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toBodilessEntity();
    }

    private boolean workerHasStatus(String status) {

        String retrieve = getRestClient().get()
                .uri("/api/sessions/default")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().body(String.class);
        return retrieve == null || retrieve.contains(status);
    }
}
