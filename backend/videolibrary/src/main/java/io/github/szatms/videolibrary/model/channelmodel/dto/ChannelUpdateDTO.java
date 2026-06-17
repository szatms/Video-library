package io.github.szatms.videolibrary.model.channelmodel.dto;

import io.github.szatms.videolibrary.model.statsmodel.ChannelStats;
import lombok.Data;

@Data
public class ChannelUpdateDTO {
    private String channelTitle;
    private String channelHandle;
    private String channelDescription;
    private ChannelStats channelStats;
}
