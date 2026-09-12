package io.github.szatms.videolibrary.model.codexmodel.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CodexSummaryResponseDTO {
    private String id;
    private String title;
    private Integer contentNumber;
    private Instant addedAt;
    private Instant updatedAt;
    private String userId;
}
