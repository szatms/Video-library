package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.notemodel.Note;
import io.github.szatms.videolibrary.model.notemodel.dto.*;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistSummaryResponseDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoSummaryResponseDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class NoteMapper {
    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================

    public NoteResponseDTO toResponseDTO(Note note){
        NoteResponseDTO dto = new NoteResponseDTO();

        dto.setId(note.getId());
        dto.setUserId(note.getUserId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setIsPdf(note.getIsPdf());
        dto.setPdfUrl(note.getPdfUrl());
        dto.setAddedAt(note.getAddedAt());
        dto.setUpdatedAt(note.getUpdatedAt());

        return dto;
    }

    public NoteSummaryResponseDTO toSummaryResponseDTO(Note note){
        NoteSummaryResponseDTO dto = new NoteSummaryResponseDTO();

        dto.setId(note.getId());
        dto.setUserId(note.getUserId());
        dto.setParentIds(note.getParentIds());
        dto.setTitle(note.getTitle());
        dto.setAddedAt(note.getAddedAt());
        dto.setUpdatedAt(note.getUpdatedAt());

        return dto;
    }

    public ParentResponseDTO toParentResponseDTO(UserVideoSummaryResponseDTO parentDTO){
        ParentResponseDTO dto = new ParentResponseDTO();

        dto.setId(parentDTO.getId());
        dto.setParentType(ParentType.VIDEO);
        dto.setTitle(parentDTO.getVideo().getTitle());
        dto.setThumbnailUrl(parentDTO.getVideo().getThumbnailUrl());
        dto.setChannelTitle(parentDTO.getVideo().getChannelTitle());
        dto.setViewCount(parentDTO.getVideo().getViewCount());
        dto.setVideoCount(null);
        dto.setInPlaylist(parentDTO.getInPlaylist());

        return dto;
    }

    public ParentResponseDTO toParentResponseDTO(UserPlaylistSummaryResponseDTO parentDTO){
        ParentResponseDTO dto = new ParentResponseDTO();

        dto.setId(parentDTO.getId());
        dto.setParentType(ParentType.PLAYLIST);
        dto.setTitle(parentDTO.getPlaylist().getTitle());
        dto.setThumbnailUrl(parentDTO.getPlaylist().getThumbnailUrl());
        dto.setChannelTitle(parentDTO.getPlaylist().getChannelTitle());
        dto.setViewCount(null);
        dto.setVideoCount(parentDTO.getPlaylist().getVideoCount());
        dto.setInPlaylist(null);

        return dto;
    }

    // =========================
    // UPDATE DTO → ENTITY
    // =========================
    public void updateNoteFromDTO(Note note, NoteUpdateDTO dto){
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());

        note.setPdfUrl(dto.getPdfUrl());

        note.setUpdatedAt(Instant.now());
    }
}
