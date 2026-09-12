package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.integration.youtube.PythonPlaylistDataProvider;
import io.github.szatms.videolibrary.integration.youtube.PythonVideoDataProvider;
import io.github.szatms.videolibrary.integration.youtube.factory.PlaylistFactory;
import io.github.szatms.videolibrary.integration.youtube.factory.VideoFactory;
import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.playlistmodel.PlaylistRepository;
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
import java.time.temporal.ChronoUnit;
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
    
    @Scheduled(cron = "${refresh.cron.expression:0 0 0 * * ?}")
    public void refreshAllVideosAndPlaylists() {
        logger.info("Starting refresh of videos and playlists");
        
        // Get update period from settings
        AppSettings appSettings = appSettingsService.getAppSettings();
        int updatePeriod = appSettings.getUpdatePeriod() != null ? appSettings.getUpdatePeriod() : 1;
        
        // Process videos in batches
        processVideosInBatches(updatePeriod);
        
        // Process playlists in batches
        processPlaylistsInBatches(updatePeriod);
        
        logger.info("Refresh process completed");
    }
    
    private void processVideosInBatches(int updatePeriod) {
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
    
    private void processPlaylistsInBatches(int updatePeriod) {
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
