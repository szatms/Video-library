package io.github.szatms.videolibrary.model.userplaylistitemmodel.dto;

import io.github.szatms.videolibrary.model.playlistitemmodel.dto.PlaylistItemResponseDTO;
import lombok.Data;

@Data
public class UserPlaylistItemResponseDTO {
    private String id;
    private String UserPlaylistId;
    private String UserVideoId;
    private PlaylistItemResponseDTO playlistItemResponseDTO;
}
