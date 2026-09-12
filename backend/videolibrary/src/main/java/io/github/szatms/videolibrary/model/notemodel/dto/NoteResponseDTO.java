package io.github.szatms.videolibrary.model.notemodel.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class NoteResponseDTO {
    private String id;
    private String userId;

    private String title;
    private String content;

    private Boolean isPdf;
    private String pdfUrl;

    private Instant addedAt;
    private Instant updatedAt;
}
