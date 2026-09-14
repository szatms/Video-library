package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.*;
import io.github.szatms.videolibrary.model.codexmodel.Codex;
import io.github.szatms.videolibrary.model.codexmodel.CodexRepository;
import io.github.szatms.videolibrary.model.codexmodel.CodexSortBy;
import io.github.szatms.videolibrary.model.codexmodel.SortDirection;
import io.github.szatms.videolibrary.model.codexmodel.dto.CodexCrudDTO;
import io.github.szatms.videolibrary.model.codexmodel.dto.ContentResponseDTO;
import io.github.szatms.videolibrary.model.notemodel.NoteRepository;
import io.github.szatms.videolibrary.model.playlistmodel.PlaylistRepository;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylistRepository;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoRepository;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import io.github.szatms.videolibrary.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodexService {
    private final StringUtils stringUtils;
    private final CodexRepository codexRepository;
    private final UserVideoRepository userVideoRepository;
    private final UserVideoMapper userVideoMapper;
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;
    private final UserPlaylistRepository userPlaylistRepository;
    private final UserPlaylistMapper userPlaylistMapper;
    private final PlaylistRepository playlistRepository;
    private final PlaylistMapper playlistMapper;
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final CodexMapper codexMapper;
    private final TrashService trashService;

    public Codex createCodex(String userId, String title, String description, List<String> contentIds){
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");

        String sanitizedTitle = stringUtils.checkTitle(title);

        Codex codex = Codex.builder()
                .userId(userId)
                .title(sanitizedTitle)
                .description(description)
                .contentIds(contentIds)
                .build();

        return codexRepository.save(codex);
    }

    public void addToCodex(String userId, CodexCrudDTO dto){
        if (checkValidity(userId, dto))
        {
            String codexId = dto.getCodexId();

            Optional<Codex> codexOptional = codexRepository.findByUserIdAndId(userId, codexId);
            if (codexOptional.isPresent()) {
                Codex codex = codexOptional.get();
                List<String> contentIds = dto.getContentIds();
                for (String contentId : contentIds) {
                    codex.getContentIds().add(contentId);
                }
                codexRepository.save(codex);
            } else {
                throw new IllegalArgumentException("Codex not found or not owned by user");
            }
        }
    }

    public void removeFromCodex(String userId, CodexCrudDTO dto){
        if (checkValidity(userId, dto)){
            String codexId = dto.getCodexId();

            Optional<Codex> codexOptional = codexRepository.findByUserIdAndId(userId, codexId);
            if (codexOptional.isPresent()) {
                Codex codex = codexOptional.get();
                List<String> contentIds = dto.getContentIds();
                for (String contentId : contentIds) {
                    codex.getContentIds().remove(contentId);
                }
                codexRepository.save(codex);
            } else {
                throw new IllegalArgumentException("Codex not found or not owned by user");
            }
        }
    }

    public void updateCodex(String userId, CodexCrudDTO dto){
        if (dto.getCodexId() == null || dto.getCodexId().isBlank()) {
            throw new IllegalArgumentException("Invalid codexId");
        }
        
        Optional<Codex> codexOptional = codexRepository.findByUserIdAndId(userId, dto.getCodexId());
        if (codexOptional.isPresent()) {
            Codex codex = codexOptional.get();
            
            // Update title if provided
            if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
                codex.setTitle(stringUtils.checkTitle(dto.getTitle()));
            }
            
            // Update description if provided
            if (dto.getDescription() != null) {
                codex.setDescription(dto.getDescription());
            }
            
            codexRepository.save(codex);
        } else {
            throw new IllegalArgumentException("Codex not found or not owned by user");
        }
    }

    public List<ContentResponseDTO> getContent(String userId, List<String> contentIds){
        List<ContentResponseDTO> contents = new ArrayList<>();

        for (String contentId:contentIds){
            userVideoRepository.findById(contentId)
                    .filter(userVideo -> userVideo.getUserId().equals(userId))
                    .ifPresent(userVideo -> {
                        contents.add(codexMapper.toContentResponseDTO(
                                userVideoMapper.toSummaryResponseDTO(
                                        userVideo,
                                        videoMapper.toSummaryDTO(videoRepository.getByVideoId(userVideo.getVideoId()))
                                )));
                    });
            userPlaylistRepository.findById(contentId)
                    .filter(userPlaylist -> userPlaylist.getUserId().equals(userId))
                    .ifPresent(userPlaylist -> {
                        contents.add(codexMapper.toContentResponseDTO(
                                userPlaylistMapper.toSummaryDTO(
                                        userPlaylist,
                                        playlistMapper.toSummaryDTO(playlistRepository.getById(userPlaylist.getPlaylistId()))
                                )));
                    });
            noteRepository.findById(contentId)
                    .filter(note -> note.getUserId().equals(userId))
                    .ifPresent(note -> {
                        contents.add(codexMapper.toContentResponseDTO(
                                noteMapper.toResponseDTO(note)
                        ));
                    });
        }

        return contents;
    }

    public Codex getCodex(String userId, String codexId){
        return codexRepository.findById(codexId)
                .filter(codex -> codex.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalStateException("Note not found"));
    }

    private boolean checkValidity(String userId, CodexCrudDTO dto){
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (dto.getCodexId() == null || dto.getCodexId().isBlank()) {
            throw new IllegalArgumentException("Invalid codexId");
        }
        return true;
    }

    public List<Codex> getCodices(
            String userId,
            CodexSortBy sortBy,
            SortDirection direction
    ) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invalid userId");
        }

        CodexSortBy effectiveSortBy = sortBy == null ? CodexSortBy.ADDED_AT : sortBy;
        SortDirection effectiveDirection = direction == null ? SortDirection.DESC : direction;

        List<Codex> codices;
        if (effectiveSortBy == CodexSortBy.NAME) {
            codices = getCodicesSortedByName(userId, effectiveDirection);
        } else {
            // For ADDED_AT, we just return the list in the proper order (default is descending by addedAt)
            codices = codexRepository.findAllByUserId(userId);
        }
        
        return codices;
    }

    private List<Codex> getCodicesSortedByName(String userId, SortDirection direction) {
        List<Codex> codices = codexRepository.findAllByUserId(userId);
        
        Comparator<Codex> comparator = Comparator.comparing(
                Codex::getTitle,
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return codices.stream()
                .sorted(comparator)
                .toList();
    }

    public void deleteCodex(String codexId){
        Codex codex = codexRepository.getById(codexId);
        String restoreId = UUID.randomUUID().toString();
        trashService.moveCodexToTrash(codex, restoreId);
    }
}
