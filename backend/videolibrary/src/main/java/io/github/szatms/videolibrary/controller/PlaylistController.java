package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.PlaylistMapper;
import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.playlistmodel.dto.PlaylistResponseDTO;
import io.github.szatms.videolibrary.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/playlists")
public class PlaylistController {
    private final PlaylistService playlistService;
    private final PlaylistMapper playlistMapper;

    @PostMapping("/import")
    public PlaylistResponseDTO importPlaylist(@RequestParam String youtubeId) {
        Playlist playlist = playlistService.getOrCreatePlaylist(youtubeId);
        return playlistMapper.toResponseDTO(playlist);
    }
}
