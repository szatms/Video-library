package io.github.szatms.videolibrary.integration.youtube.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PythonVideoResponseDTO {
    private String id;
    private String title;
    private String thumbnail;
    private String description;
    @JsonProperty("channel_id")
    private String channelId;
    @JsonProperty("uploader")
    private String channelName;
    private Long duration;
    @JsonProperty("view_count")
    private Long viewCount;
    @JsonProperty("like_count")
    private Long likeCount;
    private Long timestamp;
}
