package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.channelmodel.Channel;
import io.github.szatms.videolibrary.model.channelmodel.dto.ChannelSummaryDTO;
import io.github.szatms.videolibrary.model.statsmodel.ChannelStats;
import io.github.szatms.videolibrary.model.channelmodel.dto.ChannelResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ChannelMapper {
    public ChannelResponseDTO toChannelResponseDTO(Channel channel) {
        ChannelResponseDTO dto = new ChannelResponseDTO();

        dto.setChannelTitle(channel.getChannelTitle());
        dto.setChannelHandle(channel.getChannelHandle());
        dto.setChannelThumbnail(channel.getChannelThumbnail());
        dto.setChannelDescription(channel.getChannelDescription());
        dto.setChannelStats(channel.getChannelStats());

        return dto;
    }

    public ChannelSummaryDTO toChannelSummaryDTO(Channel channel) {
        ChannelSummaryDTO dto = new ChannelSummaryDTO();

        dto.setChannelId(channel.getChannelId());
        dto.setChannelTitle(channel.getChannelTitle());
        dto.setChannelThumbnail(channel.getChannelThumbnail());
        dto.setSubscriberCount(channel.getChannelStats().getSubCount());
        return dto;
    }
}
