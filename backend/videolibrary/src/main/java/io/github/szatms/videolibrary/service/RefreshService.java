package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.integration.youtube.PythonPlaylistDataProvider;
import io.github.szatms.videolibrary.integration.youtube.PythonVideoDataProvider;
import io.github.szatms.videolibrary.integration.youtube.factory.PlaylistFactory;
import io.github.szatms.videolibrary.integration.youtube.factory.VideoFactory;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.playlistmodel.PlaylistRepository;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylistRepository;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import io.github.szatms.videolibrary.settings.appsettings.AppSettings;
import io.github.szatms.videolibrary.settings.appsettings.AppSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RefreshService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshService.class);
    private static final int BATCH_SIZE = 100;
    
    private final VideoRepository videoRepository;
    private final PlaylistRepository playlistRepository;
    private final PythonVideoDataProvider pythonVideoDataProvider;
    private final PythonPlaylistDataProvider pythonPlaylistDataProvider;
    private final VideoFactory videoFactory;
    private final PlaylistFactory playlistFactory;
    private final AppSettingsService appSettingsService;
    private final AppSettingsRepository appSettingsRepository;
    private final UserPlaylistRepository userPlaylistRepository;

    public void manuallyRefreshAllVideosAndPlaylists(){
        logger.info("Starting refresh of videos and playlists");

        // Get update period from settings
        AppSettings appSettings = appSettingsService.getAppSettings();

        // Check if we should refresh based on contentUpdated timestamp and updatePeriod
        processVideosInBatches();

        processPlaylistsInBatches();

        updateContentUpdatedTimestamp(appSettings);


        logger.info("Refresh process completed");
    }

    @Scheduled(cron = "${refresh.cron.expression:0 0 0 * * ?}")
    public void refreshAllVideosAndPlaylists() {
        logger.info("Starting refresh of videos and playlists");
        
        // Get update period from settings
        AppSettings appSettings = appSettingsService.getAppSettings();
        
        // Check if we should refresh based on contentUpdated timestamp and updatePeriod
        if (shouldRefresh(appSettings)) {
            logger.info("Performing full refresh based on update period settings");

            processVideosInBatches();
            
            processPlaylistsInBatches();

            updateContentUpdatedTimestamp(appSettings);
        } else {
            logger.info("Skipping refresh - content was updated within the allowed period");
        }
        
        logger.info("Refresh process completed");
    }
    
    private boolean shouldRefresh(AppSettings appSettings) {
        if (appSettings == null || appSettings.getContentUpdated() == null) {
            return true; // If no timestamp exists, refresh
        }
        
        if (appSettings.getUpdatePeriod() == null) {
            return true; // If no update period configured, refresh
        }
        
        // Check if enough time has passed since last content update
        Instant now = Instant.now();
        Instant lastUpdate = appSettings.getContentUpdated();
        long secondsSinceLastUpdate = now.getEpochSecond() - lastUpdate.getEpochSecond();
        long secondsInPeriod = appSettings.getUpdatePeriod() * 24 * 60 * 60; // Convert days to seconds
        
        return secondsSinceLastUpdate >= secondsInPeriod;
    }
    
    private void updateContentUpdatedTimestamp(AppSettings appSettings) {
        if (appSettings != null) {
            appSettings.setContentUpdated(Instant.now());
            appSettingsRepository.save(appSettings);
        }
    }
    
    private void processVideosInBatches() {
        AtomicInteger totalUpdates = new AtomicInteger(0);
        AtomicInteger successfulUpdates = new AtomicInteger(0);
        Set<String> erroredVideoIds = ConcurrentHashMap.newKeySet();
        
        logger.info("Starting video refresh process");
        
        // Get videos in batches
        int page = 0;
        boolean hasMorePages = true;
        
        while (hasMorePages) {
            List<Video> videos = videoRepository.findAll()
                    .stream()
                    .skip(page * BATCH_SIZE)
                    .limit(BATCH_SIZE)
                    .toList();
            
            if (videos.isEmpty()) {
                hasMorePages = false;
                break;
            }
            
            logger.info("Processing batch {} of videos (size: {})", page + 1, videos.size());
            
            for (Video video : videos) {
                try {
                    totalUpdates.incrementAndGet();
                    String youtubeId = video.getYoutubeId();
                    
                    if (youtubeId != null) {
                        logger.debug("Refreshing video with YouTube ID: {}", youtubeId);
                        var response = pythonVideoDataProvider.load(youtubeId);
                        Video updatedVideo = videoFactory.fromItem(response);
                        updatedVideo.setVideoId(video.getVideoId());
                        videoRepository.save(updatedVideo);
                        successfulUpdates.incrementAndGet();
                        logger.debug("Successfully refreshed video: {}", youtubeId);
                    } else {
                        logger.warn("Video {} has no YouTube ID", video.getVideoId());
                        erroredVideoIds.add(video.getVideoId());
                    }
                } catch (Exception e) {
                    logger.error("Error refreshing video {}: {}", video.getVideoId(), e.getMessage(), e);
                    erroredVideoIds.add(video.getVideoId());
                }
            }
            
            page++;
        }
        
        logger.info("Video refresh completed. Total: {}, Successful: {}, Errors: {}", 
                   totalUpdates.get(), successfulUpdates.get(), erroredVideoIds.size());
        
        if (!erroredVideoIds.isEmpty()) {
            logger.warn("Errored videos: {}", erroredVideoIds);
        }
    }
    
    public void updatePlaylistAlone(String userPlaylistId) {
        String playlistId = userPlaylistRepository.getById(userPlaylistId).getPlaylistId();
        logger.info("Updating playlist alone: {}", playlistId);
        
        try {
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new RuntimeException("Playlist not found with ID: " + playlistId));
            
            String youtubeId = playlist.getYoutubeId();
            if (youtubeId == null) {
                logger.warn("Playlist {} has no YouTube ID", playlistId);
                return;
            }
            
            logger.debug("Refreshing playlist with YouTube ID: {}", youtubeId);
            var response = pythonPlaylistDataProvider.load(youtubeId);
            Playlist updatedPlaylist = playlistFactory.fromItem(response);
            updatedPlaylist.setId(playlist.getId());
            playlistRepository.save(updatedPlaylist);
            
            // Update videos in the playlist
            logger.info("Updating videos in playlist: {}", youtubeId);
            updatePlaylistVideos(playlistId, updatedPlaylist.getItems());
            
            logger.info("Successfully updated playlist and its videos: {}", youtubeId);
        } catch (Exception e) {
            logger.error("Error updating playlist {}: {}", playlistId, e.getMessage(), e);
            throw new RuntimeException("Failed to update playlist: " + playlistId, e);
        }
    }
    
    private void updatePlaylistVideos(String playlistId, List<PlaylistItem> playlistItems) {
        if (playlistItems == null || playlistItems.isEmpty()) {
            logger.info("No playlist items found for playlist: {}", playlistId);
            return;
        }
        
        logger.info("Updating {} videos in playlist: {}", playlistItems.size(), playlistId);
        
        AtomicInteger totalUpdates = new AtomicInteger(0);
        AtomicInteger successfulUpdates = new AtomicInteger(0);
        Set<String> erroredVideoIds = ConcurrentHashMap.newKeySet();
        
        for (PlaylistItem item : playlistItems) {
            try {
                totalUpdates.incrementAndGet();
                String videoId = item.getVideoId();
                
                if (videoId != null) {
                    logger.debug("Refreshing video with ID: {}", videoId);
                    var response = pythonVideoDataProvider.load(videoId);
                    Video updatedVideo = videoFactory.fromItem(response);
                    // Preserve existing video ID to overwrite the existing video
                    Video existingVideo = videoRepository.findByYoutubeId(videoId)
                            .orElse(null);
                    if (existingVideo != null) {
                        updatedVideo.setVideoId(existingVideo.getVideoId());
                    }
                    videoRepository.save(updatedVideo);
                    successfulUpdates.incrementAndGet();
                    logger.debug("Successfully refreshed video: {}", videoId);
                } else {
                    logger.warn("Playlist item in playlist {} has no video ID", playlistId);
                    erroredVideoIds.add(item.getId());
                }
            } catch (Exception e) {
                logger.error("Error refreshing video {} in playlist {}: {}", item.getVideoId(), playlistId, e.getMessage(), e);
                // For the playlist update, we don't want to fail the entire process,
                // so we continue with other videos but log the error
                erroredVideoIds.add(item.getId());
            }
        }
        
        logger.info("Video update for playlist {} completed. Total: {}, Successful: {}, Errors: {}", 
                   playlistId, totalUpdates.get(), successfulUpdates.get(), erroredVideoIds.size());
        
        if (!erroredVideoIds.isEmpty()) {
            logger.warn("Errored playlist items in playlist {}: {}", playlistId, erroredVideoIds);
        }
    }
    
    private void processPlaylistsInBatches() {
        AtomicInteger totalUpdates = new AtomicInteger(0);
        AtomicInteger successfulUpdates = new AtomicInteger(0);
        Set<String> erroredPlaylistIds = ConcurrentHashMap.newKeySet();
        
        logger.info("Starting playlist refresh process");
        
        // Get playlists in batches
        int page = 0;
        boolean hasMorePages = true;
        
        while (hasMorePages) {
            List<Playlist> playlists = playlistRepository.findAll()
                    .stream()
                    .skip(page * BATCH_SIZE)
                    .limit(BATCH_SIZE)
                    .toList();
            
            if (playlists.isEmpty()) {
                hasMorePages = false;
                break;
            }
            
            logger.info("Processing batch {} of playlists (size: {})", page + 1, playlists.size());
            
            for (Playlist playlist : playlists) {
                try {
                    totalUpdates.incrementAndGet();
                    String youtubeId = playlist.getYoutubeId();
                    
                    if (youtubeId != null) {
                        logger.debug("Refreshing playlist with YouTube ID: {}", youtubeId);
                        var response = pythonPlaylistDataProvider.load(youtubeId);
                        Playlist updatedPlaylist = playlistFactory.fromItem(response);
                        updatedPlaylist.setYoutubeId(playlist.getYoutubeId());
                        playlistRepository.save(updatedPlaylist);
                        successfulUpdates.incrementAndGet();
                        logger.debug("Successfully refreshed playlist: {}", youtubeId);
                    } else {
                        logger.warn("Playlist {} has no YouTube ID", playlist.getYoutubeId());
                        erroredPlaylistIds.add(playlist.getYoutubeId());
                    }
                } catch (Exception e) {
                    logger.error("Error refreshing playlist {}: {}", playlist.getYoutubeId(), e.getMessage(), e);
                    erroredPlaylistIds.add(playlist.getYoutubeId());
                }
            }
            
            page++;
        }
        
        logger.info("Playlist refresh completed. Total: {}, Successful: {}, Errors: {}", 
                   totalUpdates.get(), successfulUpdates.get(), erroredPlaylistIds.size());
        
        if (!erroredPlaylistIds.isEmpty()) {
            logger.warn("Errored playlists: {}", erroredPlaylistIds);
        }
    }
}
