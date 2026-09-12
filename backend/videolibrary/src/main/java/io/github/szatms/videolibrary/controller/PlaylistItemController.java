package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.PlaylistItemMapper;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import io.github.szatms.videolibrary.model.playlistitemmodel.dto.PlaylistItemResponseDTO;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.service.PlaylistItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/playlistitems")
public class PlaylistItemController {
    private final PlaylistItemService playlistItemService;
    private final PlaylistItemMapper playlistItemMapper;

    @GetMapping("/playlist/{playlistId}")
    public List<PlaylistItemResponseDTO> getPlaylistItems(
            @PathVariable String playlistId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PlaylistItem> playlistItems = playlistItemService.getAllByPlaylistId(playlistId);
        return playlistItems.stream()
                .map(playlistItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}