package io.github.szatms.videolibrary.model.videomodel.dto;

import io.github.szatms.videolibrary.model.statsmodel.Stats;
import lombok.Data;

import java.time.Instant;

@Data
public class VideoResponseDTO {
    private String videoId;
    private String youtubeId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String channelId;
    private Stats stats;
    private Instant publishedAt;
}
