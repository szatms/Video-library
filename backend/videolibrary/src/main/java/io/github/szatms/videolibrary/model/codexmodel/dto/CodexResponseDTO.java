package io.github.szatms.videolibrary.model.codexmodel.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CodexResponseDTO {
    private String id;
    private String title;
    private String description;
    private List<String> contentIds;
    private Instant addedAt;
    private Instant updatedAt;
    private String userId;
}
