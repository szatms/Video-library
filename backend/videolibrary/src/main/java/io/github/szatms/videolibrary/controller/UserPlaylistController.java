package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.PlaylistMapper;
import io.github.szatms.videolibrary.mapper.UserPlaylistMapper;
import io.github.szatms.videolibrary.model.notemodel.dto.AddToParentDTO;
import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.playlistmodel.dto.PlaylistResponseDTO;
import io.github.szatms.videolibrary.model.userplaylistmodel.SortDirection;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylist;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylistSortBy;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistCreateDTO;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistResponseDTO;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistUpdateDTO;
import io.github.szatms.videolibrary.service.NoteService;
import io.github.szatms.videolibrary.service.PlaylistService;
import io.github.szatms.videolibrary.service.UserPlaylistService;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.utils.LinkUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/userplaylists")
public class UserPlaylistController {
    private final UserPlaylistService userPlaylistService;
    private final UserPlaylistMapper userPlaylistMapper;
    private final PlaylistMapper playlistMapper;
    private final PlaylistService playlistService;
    private final LinkUtils linkUtils;
    private final NoteService noteService;

    @GetMapping
    public List<UserPlaylistResponseDTO> getPlaylists(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) UserPlaylistSortBy sortBy,
            @RequestParam(required = false) SortDirection direction
    ) {
        String userId = userDetails.getUser().getUserId();
        List<UserPlaylist> userPlaylists = userPlaylistService.getPlaylists(userId, sortBy, direction);
        return userPlaylists.stream()
                .map(userPlaylist -> {
                    Playlist playlist = playlistService.getById(userPlaylist.getPlaylistId());
                    PlaylistResponseDTO playlistResponseDTO = playlistMapper.toResponseDTO(playlist);
                    return userPlaylistMapper.toResponseDTO(userPlaylist, playlistResponseDTO);
                })
                .toList();
    }

    @PostMapping
    public UserPlaylistResponseDTO addPlaylist(
            @RequestBody UserPlaylistCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getUser().getUserId();
        String youtubeId = linkUtils.getYTVideoId(dto.getUrl());

        if (youtubeId == null) {
            throw new IllegalArgumentException("Invalid YouTube URL");
        }

        UserPlaylist userPlaylist = userPlaylistService.addPlaylist(userId, youtubeId);
        Playlist playlist = playlistService.getById(userPlaylist.getPlaylistId());
        PlaylistResponseDTO playlistResponseDTO = playlistMapper.toResponseDTO(playlist);
        return userPlaylistMapper.toResponseDTO(userPlaylist, playlistResponseDTO);
    }

    @GetMapping("/{id}")
    public UserPlaylistResponseDTO getPlaylist(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        UserPlaylist userPlaylist = userPlaylistService.getPlaylist(userId, id);
        Playlist playlist = playlistService.getById(userPlaylist.getPlaylistId());
        PlaylistResponseDTO playlistResponseDTO = playlistMapper.toResponseDTO(playlist);
        return userPlaylistMapper.toResponseDTO(userPlaylist, playlistResponseDTO);
    }

    @PostMapping("/{id}/notes/add")
    public ResponseEntity<Void> addToParent(
            @PathVariable String id,
            @RequestBody AddToParentDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        
        // Verify that the playlist belongs to the authenticated user
        userPlaylistService.getPlaylist(userId, id);
        
        // Add notes to the parent playlist
        noteService.addToParent(userId, dto);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/notes/remove")
    public ResponseEntity<Void> removeFromParent(
            @PathVariable String id,
            @RequestBody AddToParentDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        
        // Verify that the playlist belongs to the authenticated user
        userPlaylistService.getPlaylist(userId, id);
        
        // Remove notes from the parent playlist
        noteService.removeFromParent(userId, dto);
        
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public UserPlaylistResponseDTO updatePlaylist(
            @PathVariable String id,
            @RequestBody UserPlaylistUpdateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        UserPlaylist userPlaylist = userPlaylistService.updatePlaylist(userId, id, dto);
        Playlist playlist = playlistService.getById(userPlaylist.getPlaylistId());
        PlaylistResponseDTO playlistResponseDTO = playlistMapper.toResponseDTO(playlist);
        return userPlaylistMapper.toResponseDTO(userPlaylist, playlistResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        userPlaylistService.deletePlaylist(userId, id);
        return ResponseEntity.noContent().build();
    }
}
