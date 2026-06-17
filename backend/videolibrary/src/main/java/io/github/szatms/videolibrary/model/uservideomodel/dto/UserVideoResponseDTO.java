package io.github.szatms.videolibrary.model.uservideomodel.dto;

import io.github.szatms.videolibrary.model.uservideomodel.Timestamp;
import io.github.szatms.videolibrary.model.videomodel.dto.VideoResponseDTO;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserVideoResponseDTO {
    private String id;

    private boolean watched;
    private String note;
    private List<Timestamp> timestamps;

    private Instant addedAt;

    private VideoResponseDTO video;
}
