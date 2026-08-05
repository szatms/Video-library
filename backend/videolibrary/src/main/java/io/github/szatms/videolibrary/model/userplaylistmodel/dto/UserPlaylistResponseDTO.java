package io.github.szatms.videolibrary.model.userplaylistmodel.dto;

import io.github.szatms.videolibrary.model.playlistmodel.dto.PlaylistResponseDTO;
import lombok.Data;

import java.time.Instant;

@Data
public class UserPlaylistResponseDTO {
    private String id;

    private boolean watched;
    private String note;

    private Instant addedAt;

    private PlaylistResponseDTO playlist;
}
