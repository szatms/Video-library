package io.github.szatms.videolibrary.model.playlistmodel.dto;

import lombok.Data;

@Data
public class PlaylistSummaryDTO {
    private String id;
    private String youtubeId;
    private String channelId;

    private String title;
    private String channelTitle;
    private String thumbnailUrl;
    private Integer videoCount;
}
