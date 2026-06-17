package io.github.szatms.videolibrary.integration.youtube.factory;

import io.github.szatms.videolibrary.integration.youtube.dto.PythonVideoResponseDTO;
import io.github.szatms.videolibrary.model.statsmodel.Stats;
import io.github.szatms.videolibrary.model.videomodel.Video;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class VideoFactory {

    public Video fromItem(PythonVideoResponseDTO item){
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

    public void updateEntityFromItem(Video video, PythonVideoResponseDTO item) {
        if (video == null) {
            throw new IllegalArgumentException("Invalid video data!");
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

    private void validate(PythonVideoResponseDTO item) {
        if (item == null || item.getId() == null || item.getId().isBlank()) {
            throw new IllegalArgumentException("Invalid video data!");
        }
    }

    private Instant parsePublishedAt(PythonVideoResponseDTO item) {
        try {
            if (item.getTimestamp() == null) {
                return null;
            }
            return Instant.ofEpochSecond(item.getTimestamp());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractThumbnail(PythonVideoResponseDTO item) {
        return item.getThumbnail();
    }

    private Long extractDurationSeconds(PythonVideoResponseDTO item) {
        return item.getDuration();
    }

    private Stats mapStats(PythonVideoResponseDTO item) {
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
