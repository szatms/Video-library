package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.UserPlaylistMapper;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItemRepository;
import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.playlistmodel.PlaylistRepository;
import io.github.szatms.videolibrary.model.playlistmodel.dto.PlaylistSummaryDTO;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItem;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItemRepository;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistSummaryResponseDTO;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistUpdateDTO;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.usermodel.UserRepository;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylist;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPlaylistService {
    private final UserPlaylistRepository userPlaylistRepository;
    private final UserRepository userRepository;
    private final UserPlaylistMapper userPlaylistMapper;
    private final PlaylistService playlistService;
    private final UserVideoService userVideoService;
    private final UserPlaylistItemRepository userPlaylistItemRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final PlaylistRepository playlistRepository;
    private final TrashService trashService;

    public UserPlaylist addPlaylist(String userId, String youtubeId){
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (youtubeId == null) {
            throw new IllegalArgumentException("Invalid YouTube video id");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalStateException("User not found");
        }

        Playlist playlist = playlistService.getOrCreatePlaylist(youtubeId);

        if (userPlaylistRepository.findByUserIdAndPlaylistId(userId, playlist.getId()).isPresent()){
            throw new IllegalArgumentException("Playlist already added");
        }

        UserPlaylist userPlaylist = UserPlaylist.builder()
                .userId(userId)
                .playlistId(playlist.getId())
                .noteIds(new ArrayList<>())
                .watched(false)
                .addedAt(Instant.now())
                .build();

        UserPlaylist saved = userPlaylistRepository.save(userPlaylist);
        finishPlaylist(userId, playlist, userPlaylist);
        return userPlaylistRepository.save(userPlaylist);

    }

    public UserPlaylist getPlaylist(String userId, String userPlaylistId){
        return userPlaylistRepository.findById(userPlaylistId)
                .filter(up -> up.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalStateException("Playlist not found"));
    }

    public UserPlaylist updatePlaylist(String userId, String userPlaylistId, UserPlaylistUpdateDTO dto){
        UserPlaylist userPlaylist = getPlaylist(userId, userPlaylistId);
        userPlaylistMapper.updateEntityFromDTO(dto, userPlaylist);
        return userPlaylistRepository.save(userPlaylist);
    }

    public List<UserPlaylist> getPlaylists(String userId) {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");

        if (!userRepository.existsById(userId))
            throw new IllegalStateException("User not found");

        return userPlaylistRepository.findAllByUserId(userId, Sort.unsorted());
    }

    public List<UserPlaylist> getPlaylistForChannel(String userId, String channelId){
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");

        if (channelId == null || channelId.isBlank())
            throw new IllegalArgumentException("Invalid channelId");

        // First, get all playlists from this channel
        List<Playlist> channelPlaylists = playlistService.getPlaylistsByChannel(channelId);
        
        // Get the playlist IDs
        List<String> playlistIds = channelPlaylists.stream()
            .map(Playlist::getId)
            .toList();
            
        // If no playlists found for this channel, return empty list
        if (playlistIds.isEmpty()) {
            return List.of();
        }
        
        // Find user playlists that match these playlist IDs
        return userPlaylistRepository.findByUserIdAndPlaylistIds(userId, playlistIds);
    }
    
    public List<UserPlaylistSummaryResponseDTO> getPlaylistSummaryForChannel(String userId, String channelId){
        List<UserPlaylist> userPlaylists = getPlaylistForChannel(userId, channelId);
        
        return userPlaylists.stream()
            .map(userPlaylist -> {
                Playlist playlist = playlistService.getById(userPlaylist.getPlaylistId());
                PlaylistSummaryDTO playlistSummaryDTO = new PlaylistSummaryDTO();
                playlistSummaryDTO.setId(playlist.getId());
                playlistSummaryDTO.setYoutubeId(playlist.getYoutubeId());
                playlistSummaryDTO.setChannelId(playlist.getChannelId());
                playlistSummaryDTO.setTitle(playlist.getTitle());
                playlistSummaryDTO.setChannelTitle(playlist.getChannelTitle());
                playlistSummaryDTO.setThumbnailUrl(playlist.getThumbnailUrl());
                playlistSummaryDTO.setVideoCount(playlist.getVideoCount());
                
                return userPlaylistMapper.toSummaryDTO(userPlaylist, playlistSummaryDTO);
            })
            .toList();
    }

    //TODO: implement playlist deletion properly, make sure Playlists, PlaylistItems, UserPlaylistItems, UserVideos and Videos are also deleted
    public void deletePlaylist(String userId, String userPlaylistId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (userPlaylistId == null || userPlaylistId.isBlank()) {
            throw new IllegalArgumentException("Invalid userPlaylistId");
        }

        String restoreId = UUID.randomUUID().toString();
        UserPlaylist userPlaylist = getPlaylist(userId, userPlaylistId);

        // Cascading deletion of assigned items
        List<String> userVideoIds = getUserVideoIds(userPlaylistId);
        if (!userVideoIds.isEmpty()) {
            userVideoService.deleteVideos(userVideoIds, restoreId);
        }
        
        // Delete UserPlaylistItems for this playlist
        List<String> userPlaylistItemIds = userPlaylist.getItems();
        if (userPlaylistItemIds != null && !userPlaylistItemIds.isEmpty()) {
            for (String id : userPlaylistItemIds){
                UserPlaylistItem userPlaylistItem = userPlaylistItemRepository.findById(id).orElseThrow(RuntimeException::new);
                trashService.moveUserPlaylistItemToTrash(userPlaylistItem,restoreId);

                String playlistItemId = userPlaylistItem.getPlaylistItem().getId();
                long assignmentCount = userPlaylistItemRepository.countByPlaylistItemId(playlistItemId);
                if (assignmentCount == 1){
                    PlaylistItem playlistItem = playlistItemRepository.findById(playlistItemId).orElseThrow(RuntimeException::new);
                    trashService.movePlaylistItemToTrash(playlistItem, restoreId);
                    playlistItemRepository.delete(playlistItem);
                }
                userPlaylistItemRepository.delete(userPlaylistItem);
            }
        }

        // Get the playlist ID to check if it's the last reference
        String playlistId = userPlaylist.getPlaylistId();
        long assignmentCount = userPlaylistRepository.countByPlaylistId(playlistId);
        if (assignmentCount == 1){
            // This was the last reference to the playlist
            Playlist playlist = playlistRepository.getById(playlistId);
            trashService.movePlaylistToTrash(playlist, restoreId);
            playlistRepository.delete(playlist);
        }
        // Delete the UserPlaylist itself
        trashService.moveUserPlaylistToTrash(userPlaylist, restoreId);
        userPlaylistRepository.delete(userPlaylist);
    }

    public List<String> getUserVideoIds(String userPlaylistId){
        // Get all UserPlaylistItems for this UserPlaylist
        List<UserPlaylistItem> userPlaylistItems = userPlaylistItemRepository.findByUserPlaylistIdIn(List.of(userPlaylistId));
        
        // Extract the UserVideo IDs from the UserPlaylistItems
        return userPlaylistItems.stream()
                .map(UserPlaylistItem::getUserVideoId)
                .toList();
    }

    public void finishPlaylist(String userId, Playlist playlist, UserPlaylist userPlaylist){
        for (PlaylistItem item : playlist.getItems()) {
            // Add video with inPlaylist flag set to true for videos created from playlist items
            UserVideo video = userVideoService.addVideo(userId, item.getVideoId(), true);
            String userPlaylistItemId = makeUserPlaylistItems(userId, userPlaylist.getId(), video.getId(), item);
            // Add the ID to the userPlaylist's items list
            if (userPlaylist.getItems() == null) {
                userPlaylist.setItems(new ArrayList<>());
            }
            userPlaylist.getItems().add(userPlaylistItemId);
        }
    }

    public String makeUserPlaylistItems(String userId, String userPlaylistId, String userVideoId, PlaylistItem playListItem){
        UserPlaylistItem userPlaylistItem = new UserPlaylistItem();
        userPlaylistItem.setUserPlaylistId(userPlaylistId);
        userPlaylistItem.setUserVideoId(userVideoId);
        userPlaylistItem.setPlaylistItem(playListItem);
        UserPlaylistItem savedItem = userPlaylistItemRepository.save(userPlaylistItem);
        return savedItem.getId();
    }
}
