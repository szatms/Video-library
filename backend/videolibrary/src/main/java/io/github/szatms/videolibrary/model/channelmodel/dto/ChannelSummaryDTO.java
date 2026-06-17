package io.github.szatms.videolibrary.model.channelmodel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ChannelSummaryDTO {
    private String channelId;
    private String channelTitle;
    private String channelThumbnail;
    private Long subscriberCount;
}
