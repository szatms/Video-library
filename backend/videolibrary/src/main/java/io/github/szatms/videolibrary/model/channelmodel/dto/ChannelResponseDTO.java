package io.github.szatms.videolibrary.model.channelmodel.dto;

import io.github.szatms.videolibrary.model.statsmodel.ChannelStats;
import lombok.Data;

@Data
public class ChannelResponseDTO {
    private String channelTitle;
    private String channelDescription;
    private String channelHandle;
    private String channelThumbnail;
    private ChannelStats channelStats;
}
