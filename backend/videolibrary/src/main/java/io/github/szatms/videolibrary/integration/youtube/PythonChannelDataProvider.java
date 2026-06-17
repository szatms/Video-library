package io.github.szatms.videolibrary.integration.youtube;

import io.github.szatms.videolibrary.integration.youtube.dto.PythonChannelResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PythonChannelDataProvider {
    private final RestClient restClient;

    public PythonChannelResponseDTO load(String channelId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/youtube/channel")
                        .queryParam("url", channelId)
                        .build())
                .retrieve()
                .body(PythonChannelResponseDTO.class);
    }
}
