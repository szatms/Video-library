package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.dto.VideoResponseDTO;
import io.github.szatms.videolibrary.model.videomodel.dto.VideoSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class VideoMapper {
    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================
    public VideoResponseDTO toResponseDTO(Video video){
        VideoResponseDTO dto = new VideoResponseDTO();

        dto.setVideoId(video.getVideoId());
        dto.setYoutubeId(video.getYoutubeId());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setThumbnailUrl(video.getThumbnailUrl());
        dto.setChannelId(video.getChannelId());
        dto.setStats(video.getStats());
        dto.setPublishedAt(video.getPublishedAt());

        return dto;
    }

    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================
    public VideoSummaryDTO toSummaryDTO(Video video){
        VideoSummaryDTO dto = new VideoSummaryDTO();

        dto.setVideoId(video.getVideoId());
        dto.setYoutubeId(video.getYoutubeId());
        dto.setTitle(video.getTitle());
        dto.setThumbnailUrl(video.getThumbnailUrl());
        dto.setChannelId(video.getChannelId());

        return dto;
    }

    //=========================
    // UPDATE DTO --> ENTITY
    //=========================
    public void updateEntityFromDTO(VideoResponseDTO dto, Video entity){
        if (dto == null || entity == null)
            return;

        if (dto.getStats() != null)
            entity.setStats(dto.getStats());

    }
}
