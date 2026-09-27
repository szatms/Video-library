package io.github.szatms.videolibrary.model.uservideomodel.dto;

import io.github.szatms.videolibrary.model.videomodel.dto.VideoSummaryDTO;
import lombok.Data;

import java.time.Instant;

@Data
public class UserVideoSummaryResponseDTO {
    private String id;

    private boolean watched;
    private boolean hidden;
    private int noteCount;
    private Boolean inPlaylist;

    private Instant addedAt;

    private VideoSummaryDTO video;
}
