package io.github.szatms.videolibrary.model.notemodel.dto;

import lombok.Data;

@Data
public class ParentResponseDTO {
    private String id;
    private ParentType parentType;
    private String title;
    private String thumbnailUrl;
    private String channelTitle;
    private Long viewCount;
    private Integer videoCount;
    private Boolean inPlaylist;
}
