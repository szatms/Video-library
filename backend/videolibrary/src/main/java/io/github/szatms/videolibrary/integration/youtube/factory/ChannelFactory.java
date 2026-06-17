package io.github.szatms.videolibrary.integration.youtube.factory;

import io.github.szatms.videolibrary.integration.youtube.PythonChannelDataProvider;
import io.github.szatms.videolibrary.integration.youtube.dto.PythonChannelResponseDTO;
import io.github.szatms.videolibrary.integration.youtube.dto.PythonVideoResponseDTO;
import io.github.szatms.videolibrary.model.channelmodel.Channel;
import io.github.szatms.videolibrary.model.statsmodel.ChannelStats;
import io.github.szatms.videolibrary.model.statsmodel.Stats;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ChannelFactory {

    public Channel fromItem(PythonChannelResponseDTO response) {
        validate(response);

        PythonChannelResponseDTO.Item item =
                response.getItems().get(0);

        return Channel.builder()
                .channelId(item.getId())
                .channelTitle(item.getSnippet().getTitle())
                .channelDescription(item.getSnippet().getDescription())
                .channelHandle(item.getSnippet().getCustomUrl())
                .channelThumbnail(
                        item.getSnippet()
                                .getThumbnails()
                                .getHigh()
                                .getUrl()
                )
                .channelStats(mapStats(item))
                .build();
    }

    public void updateEntityFromItem(Channel channel,
                                     PythonChannelResponseDTO response) {
        if (channel == null) {
            throw new IllegalArgumentException("Invalid channel data");
        }

        validate(response);

        PythonChannelResponseDTO.Item item =
                response.getItems().get(0);

        channel.setChannelTitle(item.getSnippet().getTitle());
        channel.setChannelDescription(item.getSnippet().getDescription());
        channel.setChannelHandle(item.getSnippet().getCustomUrl());

        channel.setChannelThumbnail(
                item.getSnippet()
                        .getThumbnails()
                        .getHigh()
                        .getUrl()
        );

        channel.setChannelStats(mapStats(item));
    }

    //=========================
    // HELPER METHODS
    //=========================

    private void validate(PythonChannelResponseDTO response) {

        if (response == null
                || response.getItems() == null
                || response.getItems().isEmpty()
                || response.getItems().get(0).getId() == null
                || response.getItems().get(0).getId().isBlank()) {

            throw new IllegalArgumentException("Invalid channel data!");
        }
    }

    private ChannelStats mapStats(PythonChannelResponseDTO.Item item) {

        PythonChannelResponseDTO.Statistics statistics =
                item.getStatistics();

        return ChannelStats.builder()
                .viewCount(
                        statistics != null
                                ? statistics.getViewCount()
                                : 0L
                )
                .subCount(
                        statistics != null
                                ? statistics.getSubscriberCount()
                                : 0L
                )
                .videoCount(
                        statistics != null
                                ? statistics.getVideoCount()
                                : 0L
                )
                .updatedAt(Instant.now())
                .build();
    }

}


