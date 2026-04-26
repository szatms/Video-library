package io.github.szatms.videolibrary.integration.youtube;

import io.github.szatms.videolibrary.integration.youtube.dto.PythonVideoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.File;

@Component
@RequiredArgsConstructor
public class PythonVideoDataProvider {

    private final ObjectMapper objectMapper;

    public PythonVideoResponseDTO load() {
        try {
            return objectMapper.readValue(
                    new File("video.json"),
                    PythonVideoResponseDTO.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load video data", e);
        }
    }
}
