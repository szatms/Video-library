package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistItemService {
    private final PlaylistItemRepository playlistItemRepository;

    public List<PlaylistItem> getAllByPlaylistId(String playlistId){
        return playlistItemRepository.findByPlaylistIdIn(playlistId);
    }
}