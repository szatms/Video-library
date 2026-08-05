package io.github.szatms.videolibrary.integration.youtube.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PythonPlaylistResponseDTO {
    private String id;
    private Snippet snippet;
    private ContentDetails contentDetails;
    private Player player;
    
    // This will be used for merging with the short response
    private List<String> items;
    private PageInfo pageInfo;
    
    // For backward compatibility with factory and service code
    public String getChannelId() {
        return snippet != null ? snippet.getChannelId() : null;
    }
    
    public String getTitle() {
        return snippet != null ? snippet.getTitle() : null;
    }
    
    public String getChannelTitle() {
        return snippet != null ? snippet.getChannelTitle() : null;
    }
    
    public String getDescription() {
        return snippet != null ? snippet.getDescription() : null;
    }
    
    public String getThumbnailUrl() {
        if (snippet != null && snippet.getThumbnails() != null) {
            // Prefer maxres thumbnail if available, otherwise try others
            if (snippet.getThumbnails().getMaxres() != null) {
                return snippet.getThumbnails().getMaxres().getUrl();
            } else if (snippet.getThumbnails().getHigh() != null) {
                return snippet.getThumbnails().getHigh().getUrl();
            } else if (snippet.getThumbnails().getMedium() != null) {
                return snippet.getThumbnails().getMedium().getUrl();
            } else if (snippet.getThumbnails().getDefaultThumbnail() != null) {
                return snippet.getThumbnails().getDefaultThumbnail().getUrl();
            }
        }
        return null;
    }
    
    public Integer getVideoCount() {
        return contentDetails != null ? contentDetails.getItemCount() : null;
    }
    
    public void setVideoCount(Integer videoCount) {
        if (contentDetails == null) {
            contentDetails = new ContentDetails();
        }
        contentDetails.setItemCount(videoCount);
    }
    
    @Data
    public static class Snippet {
        private String channelId;
        private String title;
        private String channelTitle;
        private String description;
        private Thumbnails thumbnails;
    }
    
    @Data
    public static class Thumbnails {
        private Thumbnail defaultThumbnail;
        private Thumbnail medium;
        private Thumbnail high;
        private Thumbnail standard;
        private Thumbnail maxres;
    }
    
    @Data
    public static class Thumbnail {
        private String url;
    }
    
    @Data
    public static class ContentDetails {
        private Integer itemCount;
    }
    
    @Data
    public static class Player {
        private String embedHtml;
    }
    
    @Data
    public static class PageInfo {
        private Integer totalResults;
        private Integer resultsPerPage;
    }
}
