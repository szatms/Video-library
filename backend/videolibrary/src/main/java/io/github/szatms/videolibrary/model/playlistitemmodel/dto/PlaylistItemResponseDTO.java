package io.github.szatms.videolibrary.model.playlistitemmodel.dto;

import lombok.Data;

@Data
public class PlaylistItemResponseDTO {
    private String id;
    private String playlistId;
    private String videoId;
    private int position;
}
