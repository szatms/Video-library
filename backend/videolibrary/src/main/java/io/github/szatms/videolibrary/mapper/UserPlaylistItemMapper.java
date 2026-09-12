package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.playlistitemmodel.dto.PlaylistItemResponseDTO;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItem;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.dto.UserPlaylistItemResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserPlaylistItemMapper {
    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================
    public UserPlaylistItemResponseDTO toResponseDTO(UserPlaylistItem userPlaylistItem, PlaylistItemResponseDTO playlistItem){
        UserPlaylistItemResponseDTO dto = new UserPlaylistItemResponseDTO();

        dto.setId(userPlaylistItem.getId());
        dto.setUserPlaylistId(userPlaylistItem.getUserPlaylistId());
        dto.setUserVideoId(userPlaylistItem.getUserVideoId());
        dto.setPlaylistItemResponseDTO(playlistItem);

        return dto;
    }
}
