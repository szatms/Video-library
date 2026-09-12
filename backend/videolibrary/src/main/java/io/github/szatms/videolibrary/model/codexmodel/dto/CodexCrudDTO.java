package io.github.szatms.videolibrary.model.codexmodel.dto;

import lombok.Data;

import java.util.List;

@Data
public class CodexCrudDTO {
    private String codexId;
    private String title;
    private String description;
    private List<String> contentIds;
    private String userId;
}
