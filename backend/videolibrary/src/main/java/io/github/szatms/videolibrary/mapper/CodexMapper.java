package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.codexmodel.Codex;
import io.github.szatms.videolibrary.model.codexmodel.ContentType;
import io.github.szatms.videolibrary.model.codexmodel.dto.CodexResponseDTO;
import io.github.szatms.videolibrary.model.codexmodel.dto.CodexSummaryResponseDTO;
import io.github.szatms.videolibrary.model.codexmodel.dto.ContentResponseDTO;
import io.github.szatms.videolibrary.model.notemodel.dto.NoteResponseDTO;
import io.github.szatms.videolibrary.model.notemodel.dto.NoteSummaryResponseDTO;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistSummaryResponseDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoSummaryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CodexMapper {
    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================
    public CodexResponseDTO toCodexResponseDTO(Codex codex) {
        CodexResponseDTO dto = new CodexResponseDTO();

        dto.setId(codex.getId());
        dto.setTitle(codex.getTitle());
        dto.setDescription(codex.getDescription());
        dto.setContentIds(codex.getContentIds());
        dto.setAddedAt(codex.getAddedAt());
        dto.setUpdatedAt(codex.getUpdatedAt());
        dto.setUserId(codex.getUserId());

        return dto;
    }

    public CodexSummaryResponseDTO toCodexSummaryResponseDTO(Codex codex){
        CodexSummaryResponseDTO dto = new CodexSummaryResponseDTO();

        dto.setId(codex.getId());
        dto.setTitle(codex.getTitle());
        dto.setContentNumber(codex.getContentIds().size());
        dto.setAddedAt(codex.getAddedAt());
        dto.setUpdatedAt(codex.getUpdatedAt());
        dto.setUserId(codex.getUserId());

        return dto;
    }

    public ContentResponseDTO toContentResponseDTO(UserVideoSummaryResponseDTO contentDto){
        ContentResponseDTO dto = new ContentResponseDTO();

        dto.setId(contentDto.getId());
        dto.setContentType(ContentType.VIDEO);
        dto.setTitle(contentDto.getVideo().getTitle());
        dto.setThumbnailUrl(contentDto.getVideo().getThumbnailUrl());
        dto.setChannelTitle(contentDto.getVideo().getChannelTitle());
        dto.setViewCount(contentDto.getVideo().getViewCount());
        dto.setVideoCount(null);
        dto.setInPlaylist(contentDto.getInPlaylist());

        return dto;
    }

    public ContentResponseDTO toContentResponseDTO(UserPlaylistSummaryResponseDTO contentDto){
        ContentResponseDTO dto = new ContentResponseDTO();

        dto.setId(contentDto.getId());
        dto.setContentType(ContentType.PLAYLIST);
        dto.setTitle(contentDto.getPlaylist().getTitle());
        dto.setThumbnailUrl(contentDto.getPlaylist().getThumbnailUrl());
        dto.setChannelTitle(contentDto.getPlaylist().getChannelTitle());
        dto.setViewCount(null);
        dto.setVideoCount(contentDto.getPlaylist().getVideoCount());
        dto.setInPlaylist(null);

        return dto;
    }

    public ContentResponseDTO toContentResponseDTO(NoteResponseDTO contentDto){
        ContentResponseDTO dto = new ContentResponseDTO();

        dto.setId(contentDto.getId());
        dto.setContentType(ContentType.NOTE);
        dto.setTitle(contentDto.getTitle());
        dto.setNoteContent(contentDto.getContent());
        dto.setThumbnailUrl(null);
        dto.setChannelTitle(null);
        dto.setViewCount(null);
        dto.setVideoCount(null);
        dto.setInPlaylist(null);

        return dto;
    }
}
