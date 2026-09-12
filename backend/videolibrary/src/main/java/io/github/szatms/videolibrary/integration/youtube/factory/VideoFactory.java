package io.github.szatms.videolibrary.integration.youtube.factory;

import io.github.szatms.videolibrary.integration.youtube.dto.PythonVideoResponseDTO;
import io.github.szatms.videolibrary.model.statsmodel.Stats;
import io.github.szatms.videolibrary.model.videomodel.Video;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class VideoFactory {

    public Video fromItem(PythonVideoResponseDTO response){
        // Extract the first item from the response
        PythonVideoResponseDTO.Item item = null;
        if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {
            item = response.getItems().get(0);
        }
        
        validate(item);

        return Video.builder()
                .youtubeId(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .thumbnailUrl(extractThumbnail(item))
                .channelId(item.getChannelId())
                .channelTitle(item.getChannelName())
                .durationSeconds(extractDurationSeconds(item))
                .publishedAt(parsePublishedAt(item))
                .stats(mapStats(item))
                .build();
    }

    public void updateEntityFromItem(Video video, PythonVideoResponseDTO response) {
        if (video == null) {
            throw new IllegalArgumentException("Invalid video data!");
        }
        
        // Extract the first item from the response
        PythonVideoResponseDTO.Item item = null;
        if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {
            item = response.getItems().get(0);
        }
        
        validate(item);

        video.setYoutubeId(item.getId());
        video.setTitle(item.getTitle());
        video.setDescription(item.getDescription());
        video.setThumbnailUrl(extractThumbnail(item));
        video.setChannelId(item.getChannelId());
        video.setChannelTitle(item.getChannelName());
        video.setDurationSeconds(extractDurationSeconds(item));
        video.setPublishedAt(parsePublishedAt(item));
        video.setStats(mapStats(item));
    }

    //=========================
    // HELPER METHODS
    //=========================

    private void validate(PythonVideoResponseDTO.Item item) {
        if (item == null || item.getId() == null || item.getId().isBlank()) {
            throw new IllegalArgumentException("Invalid video data!");
        }
    }

    private Instant parsePublishedAt(PythonVideoResponseDTO.Item item) {
        try {
            if (item.getTimestamp() == null) {
                return null;
            }
            return Instant.ofEpochSecond(item.getTimestamp());
        } catch (Exception e) {
            return null;
        }
    }

    private String extractThumbnail(PythonVideoResponseDTO.Item item) {
        return item.getThumbnail();
    }

    private Long extractDurationSeconds(PythonVideoResponseDTO.Item item) {
        return item.getDuration();
    }

    private Stats mapStats(PythonVideoResponseDTO.Item item) {
        return Stats.builder()
                .viewCount(parseLong(item.getViewCount()))
                .likeCount(parseLong(item.getLikeCount()))
                .updatedAt(Instant.now())
                .build();
    }

    private long parseLong(Long value) {
        return value != null ? value : 0L;
    }
}
