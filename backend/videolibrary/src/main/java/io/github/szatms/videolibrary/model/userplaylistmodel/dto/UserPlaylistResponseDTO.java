package io.github.szatms.videolibrary.model.userplaylistmodel.dto;

import io.github.szatms.videolibrary.model.playlistmodel.dto.PlaylistResponseDTO;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItem;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserPlaylistResponseDTO {
    private String id;

    private boolean watched;
    private List<String> noteIds;
    private List<UserPlaylistItem> items;

    private Instant addedAt;

    private PlaylistResponseDTO playlist;
}
