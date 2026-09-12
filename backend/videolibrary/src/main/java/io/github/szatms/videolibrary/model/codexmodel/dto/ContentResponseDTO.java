package io.github.szatms.videolibrary.model.codexmodel.dto;

import io.github.szatms.videolibrary.model.codexmodel.ContentType;
import lombok.Data;

@Data
public class ContentResponseDTO {
    private String id;
    private ContentType contentType;
    private String title;
    private String thumbnailUrl;
    private String channelTitle;
    private Long viewCount;
    private Integer videoCount;
    private Boolean inPlaylist;
    private String noteContent;
}
