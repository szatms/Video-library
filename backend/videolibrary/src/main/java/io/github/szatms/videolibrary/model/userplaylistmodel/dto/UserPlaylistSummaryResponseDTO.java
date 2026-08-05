package io.github.szatms.videolibrary.model.userplaylistmodel.dto;

import io.github.szatms.videolibrary.model.playlistmodel.dto.PlaylistSummaryDTO;
import lombok.Data;

import java.time.Instant;

@Data
public class UserPlaylistSummaryResponseDTO {
    private String id;

    private boolean watched;

    private Instant addedAt;

    private PlaylistSummaryDTO playlist;
}
