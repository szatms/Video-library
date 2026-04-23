package io.github.szatms.videolibrary.model.uservideomodel.dto;

import io.github.szatms.videolibrary.model.videomodel.dto.VideoResponseDTO;
import lombok.Data;

import java.time.Instant;

@Data
public class UserVideoResponseDTO {
    private String UVid;
    private String userId;

    private boolean watched;
    private Instant addedAt;

    private VideoResponseDTO video;
}
