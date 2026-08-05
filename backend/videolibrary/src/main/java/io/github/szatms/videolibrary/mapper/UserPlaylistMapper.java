package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.playlistmodel.dto.PlaylistResponseDTO;
import io.github.szatms.videolibrary.model.playlistmodel.dto.PlaylistSummaryDTO;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylist;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistResponseDTO;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistSummaryResponseDTO;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistUpdateDTO;
import org.springframework.stereotype.Component;

@Component
public class UserPlaylistMapper {
    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================
    public UserPlaylistResponseDTO toResponseDTO(UserPlaylist userPlaylist, PlaylistResponseDTO playlistResponseDTO){
        UserPlaylistResponseDTO dto = new UserPlaylistResponseDTO();

        dto.setId(userPlaylist.getId());
        dto.setWatched(userPlaylist.isWatched());
        dto.setNote(userPlaylist.getNote());
        dto.setAddedAt(userPlaylist.getAddedAt());
        dto.setPlaylist(playlistResponseDTO);

        return dto;
    }

    public UserPlaylistSummaryResponseDTO toSummaryDTO(UserPlaylist userPlaylist, PlaylistSummaryDTO playlistSummaryDTO){
        UserPlaylistSummaryResponseDTO dto = new UserPlaylistSummaryResponseDTO();

        dto.setId(userPlaylist.getId());
        dto.setWatched(userPlaylist.isWatched());
        dto.setAddedAt(userPlaylist.getAddedAt());
        dto.setPlaylist(playlistSummaryDTO);

        return dto;
    }

    //=========================
    // DTO --> ENTITY
    //=========================
    public void updateEntityFromDTO(UserPlaylistUpdateDTO dto, UserPlaylist entity){
        if (dto == null || entity == null)
            return;

        if (dto.getNote() != null) {
            String note = dto.getNote().trim();
            entity.setNote(note.isEmpty() ? null : note);
        }

        entity.setWatched(dto.isWatched());
    }
}