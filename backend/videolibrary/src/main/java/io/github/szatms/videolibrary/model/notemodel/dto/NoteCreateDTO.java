package io.github.szatms.videolibrary.model.notemodel.dto;

import lombok.Data;

import java.util.List;

@Data
public class NoteCreateDTO {
    private String userId;
    private List<String> parentIds;

    private String title;
    private String content;

    private Boolean isPdf;
    private String pdfUrl;
}
