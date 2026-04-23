package io.github.szatms.videolibrary.model.videomodel.dto;

import lombok.Data;

@Data
public class VideoSummaryDTO {
    private String videoId;
    private String youtubeId;
    private String title;
    private String thumbnailUrl;
    private String channelId;
}
