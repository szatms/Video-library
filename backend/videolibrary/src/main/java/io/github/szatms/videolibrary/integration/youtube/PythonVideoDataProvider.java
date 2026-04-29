package io.github.szatms.videolibrary.integration.youtube;

import io.github.szatms.videolibrary.integration.youtube.dto.PythonVideoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PythonVideoDataProvider {

    private final RestClient restClient;

    public PythonVideoResponseDTO load(String youtubeId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/youtube/video")
                        .queryParam("url", youtubeId)
                        .build())
                .retrieve()
                .body(PythonVideoResponseDTO.class);
    }
}
