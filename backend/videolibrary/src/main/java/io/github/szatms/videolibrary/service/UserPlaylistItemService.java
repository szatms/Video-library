package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItemRepository;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItem;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPlaylistItemService {
    private final UserPlaylistItemRepository userPlaylistItemRepository;
    private final PlaylistItemRepository playlistItemRepository;

    public List<UserPlaylistItem> getAllByPlaylistId(String userPlaylistId){
        if (userPlaylistItemRepository == null) {
            throw new IllegalStateException("UserPlaylistItemRepository is not properly injected");
        }
        // Check if userPlaylistId is null or empty
        if (userPlaylistId == null || userPlaylistId.isEmpty()) {
            return List.of();
        }
        return userPlaylistItemRepository.findByUserPlaylistIdIn(List.of(userPlaylistId));
    }

    public void deleteItems(List<String> ids){
        //TODO finish function - done?
        List<PlaylistItem> playlistItemsToDelete = new ArrayList<>();
        List<UserPlaylistItem> userPlaylistItemsToDelete = new ArrayList<>();
        for (String id : ids) {
            userPlaylistItemRepository.findById(id)
                    .ifPresent(item -> {
                        // Check if the playlist item is valid and has an ID
                        if (item.getPlaylistItem() != null && item.getPlaylistItem().getId() != null) {
                            String playlistItemId = item.getPlaylistItem().getId();
                            long count = userPlaylistItemRepository.countByPlaylistItemId(playlistItemId);
                            
                            if (count == 1) {
                                playlistItemsToDelete.add(item.getPlaylistItem());
                                userPlaylistItemsToDelete.add(item);
                            } else {
                                userPlaylistItemRepository.deleteById(id);
                            }
                        } else {
                            // PlaylistItem is invalid, just delete the UserPlaylistItem
                            userPlaylistItemRepository.deleteById(id);
                        }
                    });
        }
        playlistItemRepository.deleteAll(playlistItemsToDelete);
        userPlaylistItemRepository.deleteAll(userPlaylistItemsToDelete);
    }

}
