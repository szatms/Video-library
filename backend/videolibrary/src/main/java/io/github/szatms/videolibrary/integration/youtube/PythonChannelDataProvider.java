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
        // Validate input
        if (channelId == null || channelId.isBlank()) {
            throw new IllegalArgumentException("Channel ID cannot be null or empty");
        }
        
        // Construct full YouTube channel URL from channel ID
        String channelUrl = "https://www.youtube.com/channel/" + channelId;
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/youtube/channel")
                        .queryParam("url", channelUrl)
                        .build())
                .retrieve()
                .body(PythonChannelResponseDTO.class);
    }
}
