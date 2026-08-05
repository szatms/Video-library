package io.github.szatms.videolibrary.integration.youtube.factory;

import io.github.szatms.videolibrary.integration.youtube.dto.PythonPlaylistResponseDTO;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItemRepository;
import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.service.VideoService;
import io.github.szatms.videolibrary.utils.LinkUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlaylistFactory {

    private final LinkUtils linkUtils;
    private final VideoService videoService;
    private final PlaylistItemRepository playlistItemRepository;

    public PlaylistFactory(LinkUtils linkUtils, VideoService videoService, PlaylistItemRepository playlistItemRepository) {
        this.linkUtils = linkUtils;
        this.videoService = videoService;
        this.playlistItemRepository = playlistItemRepository;
    }

    public Playlist fromItem(PythonPlaylistResponseDTO item){
        validate(item);

        // Extract nested fields from the DTO structure
        String channelId = null;
        String title = null;
        String channelTitle = null;
        String description = null;
        String thumbnailUrl = null;
        Integer videoCount = null;
        
        if (item.getSnippet() != null) {
            channelId = item.getSnippet().getChannelId();
            title = item.getSnippet().getTitle();
            channelTitle = item.getSnippet().getChannelTitle();
            description = item.getSnippet().getDescription();
        }
        
        if (item.getContentDetails() != null) {
            videoCount = item.getContentDetails().getItemCount();
        }
        
        // Try to get thumbnail from various sizes
        if (item.getSnippet() != null && item.getSnippet().getThumbnails() != null) {
            // Prefer maxres thumbnail if available, otherwise try others
            if (item.getSnippet().getThumbnails().getMaxres() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getMaxres().getUrl();
            } else if (item.getSnippet().getThumbnails().getHigh() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getHigh().getUrl();
            } else if (item.getSnippet().getThumbnails().getMedium() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getMedium().getUrl();
            } else if (item.getSnippet().getThumbnails().getDefaultThumbnail() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getDefaultThumbnail().getUrl();
            }
        }

        return Playlist.builder()
                .youtubeId(item.getId())
                .channelId(channelId)
                .title(title)
                .channelTitle(channelTitle)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .videoCount(videoCount)
                .items(makeItems(item.getItems(), item.getId()))
                .build();
    }

    public void updatePlaylistFromItem(Playlist playlist, PythonPlaylistResponseDTO item){
        if (playlist == null)
                throw new IllegalArgumentException("Invalid playlist data!");
        validate(item);

        // Extract nested fields from the DTO structure
        String channelId = null;
        String title = null;
        String channelTitle = null;
        String description = null;
        String thumbnailUrl = null;
        Integer videoCount = null;
        
        if (item.getSnippet() != null) {
            channelId = item.getSnippet().getChannelId();
            title = item.getSnippet().getTitle();
            channelTitle = item.getSnippet().getChannelTitle();
            description = item.getSnippet().getDescription();
        }
        
        if (item.getContentDetails() != null) {
            videoCount = item.getContentDetails().getItemCount();
        }
        
        // Try to get thumbnail from various sizes
        if (item.getSnippet() != null && item.getSnippet().getThumbnails() != null) {
            // Prefer maxres thumbnail if available, otherwise try others
            if (item.getSnippet().getThumbnails().getMaxres() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getMaxres().getUrl();
            } else if (item.getSnippet().getThumbnails().getHigh() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getHigh().getUrl();
            } else if (item.getSnippet().getThumbnails().getMedium() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getMedium().getUrl();
            } else if (item.getSnippet().getThumbnails().getDefaultThumbnail() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getDefaultThumbnail().getUrl();
            }
        }

        playlist.setYoutubeId(item.getId());
        playlist.setChannelId(channelId);
        playlist.setTitle(title);
        playlist.setChannelTitle(channelTitle);
        playlist.setDescription(description);
        playlist.setThumbnailUrl(thumbnailUrl);
        playlist.setVideoCount(videoCount);
        playlist.setItems(makeItems(item.getItems(), item.getId()));
    }

    //=========================
    // HELPER METHODS
    //=========================

    private void validate(PythonPlaylistResponseDTO item){
        if (item == null || item.getId() == null || item.getId().isBlank()){
            throw new IllegalArgumentException("Invalid playlist data!");
        }
    }

    private List<PlaylistItem> makeItems(List<String> links, String playlistId){
        List<PlaylistItem> playlistItems = new ArrayList<>();

        if (links == null) {
            return playlistItems;
        }

        for (int i = 0; i < links.size(); i++) {
            String link = links.get(i);
            String videoId = linkUtils.getYTVideoId(link);
            
            if (videoId != null) {
                // Fetch or create the video entity
                videoService.getOrCreateVideo(videoId);
                
                // Create PlaylistItem with the video ID and position
                PlaylistItem item = PlaylistItem.builder()
                        .playlistId(playlistId)
                        .videoId(videoId)
                        .position(i)
                        .build();
                
                playlistItems.add(item);
                playlistItemRepository.save(item);
            }
        }

        return playlistItems;
    }
}
