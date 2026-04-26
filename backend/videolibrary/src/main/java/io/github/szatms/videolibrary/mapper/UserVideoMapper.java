package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.usermodel.dto.UserResponseDTO;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoResponseDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoUpdateDTO;
import io.github.szatms.videolibrary.model.videomodel.dto.VideoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserVideoMapper {
    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================
    public UserVideoResponseDTO toResponseDTO(UserVideo userVideo, VideoResponseDTO videoDto) {
        UserVideoResponseDTO dto = new UserVideoResponseDTO();

        dto.setId(userVideo.getId());
        dto.setWatched(userVideo.isWatched());
        dto.setNote(userVideo.getNote());
        dto.setAddedAt(userVideo.getAddedAt());
        dto.setVideo(videoDto);
        return dto;
    }

    // =========================
    // UPDATE DTO → ENTITY
    // =========================
    public void updateEntityFromDTO(UserVideoUpdateDTO dto, UserVideo entity) {
        if (dto == null || entity == null)
            return;

        if (dto.getNote() != null) {
            String note = dto.getNote().trim();
            entity.setNote(note.isEmpty() ? null : note);
        }

        entity.setWatched(dto.isWatched());
    }
}
