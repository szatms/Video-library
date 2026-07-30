package io.github.szatms.videolibrary.model.playlistmodel.dto;

import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import lombok.Data;

import java.util.List;

@Data
public class PlaylistResponseDTO {
    private String id;
    private String youtubeId;
    private String channelId;

    private String title;
    private String channelTitle;
    private String description;
    private String thumbnailUrl;
    private Integer videoCount;
    private List<PlaylistItem> items;
}
