package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.playlistmodel.dto.PlaylistResponseDTO;
import io.github.szatms.videolibrary.model.playlistmodel.dto.PlaylistSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class PlaylistMapper {
    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================
    public PlaylistResponseDTO toResponseDTO(Playlist playlist){
        PlaylistResponseDTO dto = new PlaylistResponseDTO();

        dto.setId(playlist.getId());
        dto.setYoutubeId(playlist.getYoutubeId());
        dto.setChannelId(playlist.getChannelId());

        dto.setTitle(playlist.getTitle());
        dto.setChannelTitle(playlist.getChannelTitle());
        dto.setDescription(playlist.getDescription());
        dto.setThumbnailUrl(playlist.getThumbnailUrl());
        dto.setItems(playlist.getItems());

        return dto;
    }

    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================
    public PlaylistSummaryDTO toSummaryDTO(Playlist playlist){
        PlaylistSummaryDTO dto = new PlaylistSummaryDTO();

        dto.setId(playlist.getId());
        dto.setYoutubeId(playlist.getYoutubeId());
        dto.setChannelId(playlist.getChannelId());

        dto.setTitle(playlist.getTitle());
        dto.setChannelTitle(playlist.getChannelTitle());
        dto.setThumbnailUrl(playlist.getThumbnailUrl());
        dto.setVideoCount(playlist.getVideoCount());

        return dto;
    }
}
