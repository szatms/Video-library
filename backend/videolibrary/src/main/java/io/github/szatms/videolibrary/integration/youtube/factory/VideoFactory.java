package io.github.szatms.videolibrary.integration.youtube.factory;

import io.github.szatms.videolibrary.integration.youtube.dto.PythonVideoResponseDTO;
import io.github.szatms.videolibrary.model.statsmodel.Stats;
import io.github.szatms.videolibrary.model.videomodel.Video;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class VideoFactory {

    public Video fromItem(PythonVideoResponseDTO.Item item){
        if (item == null || item.getSnippet() == null) throw new IllegalArgumentException("Invalid video data!");

        return Video.builder()
                .youtubeId(item.getId())
                .title(item.getSnippet().getTitle())
                .description(item.getSnippet().getDescription())
                .thumbnailUrl(extractThumbnail(item))
                .channelId(item.getSnippet().getChannelId())
                .publishedAt(parsePublishedAt(item))
                .stats(mapStats(item.getStatistics()))
                .build();
    }

    public void updateEntityFromItem(Video video, PythonVideoResponseDTO.Item item) {
        if (video == null || item == null || item.getSnippet() == null) {
            throw new IllegalArgumentException("Invalid video data!");
        }

        video.setYoutubeId(item.getId());
        video.setTitle(item.getSnippet().getTitle());
        video.setDescription(item.getSnippet().getDescription());
        video.setThumbnailUrl(extractThumbnail(item));
        video.setChannelId(item.getSnippet().getChannelId());
        video.setPublishedAt(parsePublishedAt(item));
        video.setStats(mapStats(item.getStatistics()));
    }

    //=========================
    // HELPER METHODS
    //=========================
    private Instant parsePublishedAt(PythonVideoResponseDTO.Item item) {
        try {
            return Instant.parse(item.getSnippet().getPublishedAt());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractThumbnail(PythonVideoResponseDTO.Item item) {
        if (item.getSnippet().getThumbnails() == null) return null;
        if (item.getSnippet().getThumbnails().getHigh() == null) return null;

        return item.getSnippet().getThumbnails().getHigh().getUrl();
    }

    private Stats mapStats(PythonVideoResponseDTO.Statistics stats) {
        if (stats == null) return null;

        return Stats.builder()
                .viewCount(parseLong(stats.getViewCount()))
                .likeCount(parseLong(stats.getLikeCount()))
                .updatedAt(Instant.now())
                .build();
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0;
        }
    }
}
