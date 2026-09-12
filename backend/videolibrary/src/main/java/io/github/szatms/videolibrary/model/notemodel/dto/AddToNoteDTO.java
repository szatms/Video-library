package io.github.szatms.videolibrary.model.notemodel.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddToNoteDTO {
    private String noteId;
    private List<String> parentIds;
}
