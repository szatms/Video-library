package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.PlaylistItemMapper;
import io.github.szatms.videolibrary.mapper.UserPlaylistItemMapper;
import io.github.szatms.videolibrary.model.playlistitemmodel.dto.PlaylistItemResponseDTO;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItem;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.dto.UserPlaylistItemResponseDTO;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.service.UserPlaylistItemService;
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
@RequestMapping("/api/userplaylistitems")
public class UserPlaylistItemController {
    private final UserPlaylistItemService userPlaylistItemService;
    private final UserPlaylistItemMapper userPlaylistItemMapper;
    private final PlaylistItemMapper playlistItemMapper;

    @GetMapping("/userplaylist/{userPlaylistId}")
    public List<UserPlaylistItemResponseDTO> getUserPlaylistItems(
            @PathVariable String userPlaylistId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        List<UserPlaylistItem> userPlaylistItems = userPlaylistItemService.getAllByPlaylistId(userPlaylistId);
        return userPlaylistItems.stream()
                .map(userPlaylistItem -> {
                    // Convert the embedded playlistItem to DTO
                    PlaylistItemResponseDTO playlistItemDTO = playlistItemMapper.toResponseDTO(userPlaylistItem.getPlaylistItem());
                    return userPlaylistItemMapper.toResponseDTO(userPlaylistItem, playlistItemDTO);
                })
                .collect(Collectors.toList());
    }
}
