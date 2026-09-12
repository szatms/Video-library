package io.github.szatms.videolibrary.model.notemodel.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class NoteSummaryResponseDTO {
    private String id;
    private String userId;
    private List<String> parentIds;

    private String title;

    private Instant addedAt;
    private Instant updatedAt;
}
