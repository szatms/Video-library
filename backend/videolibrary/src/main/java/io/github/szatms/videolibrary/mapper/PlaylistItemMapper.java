package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import io.github.szatms.videolibrary.model.playlistitemmodel.dto.PlaylistItemResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class PlaylistItemMapper {
    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================
    public PlaylistItemResponseDTO toResponseDTO (PlaylistItem playlistItem){
        PlaylistItemResponseDTO dto = new PlaylistItemResponseDTO();

        dto.setId(playlistItem.getId());
        dto.setPlaylistId(playlistItem.getPlaylistId());
        dto.setVideoId(playlistItem.getVideoId());
        dto.setPosition(playlistItem.getPosition());

        return dto;
    }
}
