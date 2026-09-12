package io.github.szatms.videolibrary.model.notemodel.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddToParentDTO {
    String parentId;
    List<String> noteIds;
}
