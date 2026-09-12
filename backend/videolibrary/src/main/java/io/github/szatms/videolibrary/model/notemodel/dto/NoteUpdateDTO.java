package io.github.szatms.videolibrary.model.notemodel.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class NoteUpdateDTO {
    private String title;
    private String content;

    private String pdfUrl;
}
