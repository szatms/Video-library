package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.PlaylistMapper;
import io.github.szatms.videolibrary.mapper.UserPlaylistMapper;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItemRepository;
import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.userplaylistmodel.dto.UserPlaylistUpdateDTO;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoRepository;
import io.github.szatms.videolibrary.model.usermodel.UserRepository;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylist;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylistRepository;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoUpdateDTO;
import io.github.szatms.videolibrary.utils.LinkUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import java.time.Instant;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserPlaylistService {
    private final UserPlaylistRepository userPlaylistRepository;
    private final UserRepository userRepository;
    private final UserPlaylistMapper userPlaylistMapper;
    private final PlaylistService playlistService;
    private final UserVideoService userVideoService;
    private final PlaylistMapper playlistMapper;
    private final UserVideoRepository userVideoRepository;
    private final PlaylistItemRepository playlistItemRepository;

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
                .note(null)
                .watched(false)
                .addedAt(Instant.now())
                .build();

        UserPlaylist saved = userPlaylistRepository.save(userPlaylist);
        makePlaylistVideos(userId, playlist);
        return saved;

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
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalStateException("User not found");
        }
        return userPlaylistRepository.findAllByUserId(userId, Sort.unsorted());
    }

    public void deletePlaylist(String userId, String userPlaylistId) {
        UserPlaylist userPlaylist = getPlaylist(userId, userPlaylistId);
        
        // Get the playlist ID to check if it's the last reference
        String playlistId = userPlaylist.getPlaylistId();
        
        // Delete the UserPlaylist entry
        userPlaylistRepository.deleteById(userPlaylistId);
        
        // Check if this was the last reference to this playlist
        // We count how many UserPlaylist entries reference this playlist
        if (userPlaylistRepository.countByPlaylistId(playlistId) == 0) {
            // This was the last UserPlaylist referencing this playlist
            // Find all playlist items for this playlist
            List<PlaylistItem> playlistItems = playlistItemRepository.findByPlaylistIdIn(playlistId);
            
            // Get all video IDs from these playlist items
            List<String> videoIds = playlistItems.stream()
                    .map(PlaylistItem::getVideoId)
                    .toList();
            
            // Delete all PlaylistItems for this playlist
            playlistItemRepository.deleteAll(playlistItems);
            
            // Delete the playlist itself
            playlistService.deleteById(playlistId);
            
            // Delete all UserVideo entries that were created from this playlist
            // (These are the videos that had inPlaylist = true)
            if (!videoIds.isEmpty()) {
                // We need to delete UserVideo entries that match these video IDs and were created from playlists
                // Since UserVideoRepository doesn't have a method for this, we'll do it manually
                List<UserVideo> allUserVideos = userVideoRepository.findAll();
                List<UserVideo> userVideosToDelete = allUserVideos.stream()
                        .filter(userVideo -> videoIds.contains(userVideo.getVideoId()) && userVideo.isInPlaylist())
                        .toList();
                
                if (!userVideosToDelete.isEmpty()) {
                    userVideoRepository.deleteAll(userVideosToDelete);
                }
            }
        }
    }

    public void makePlaylistVideos(String userId, Playlist playlist){
        for (PlaylistItem item : playlist.getItems()) {
            UserVideo userVideo = userVideoService.addVideo(userId, item.getVideoId());
            // Set inPlaylist flag to true for videos created from playlist items
            userVideo.setInPlaylist(true);
            userVideoRepository.save(userVideo);
        }
    }
}
