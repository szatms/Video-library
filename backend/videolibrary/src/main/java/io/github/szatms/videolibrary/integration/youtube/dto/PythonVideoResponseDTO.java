package io.github.szatms.videolibrary.integration.youtube.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PythonVideoResponseDTO {
    private List<Item> items;
    private PageInfo pageInfo;
    
    // Delegating methods to the first item in the list
    public String getId() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getId();
        }
        return null;
    }
    
    public String getTitle() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getTitle();
        }
        return null;
    }
    
    public String getThumbnail() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getThumbnail();
        }
        return null;
    }
    
    public String getDescription() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getDescription();
        }
        return null;
    }
    
    public String getChannelId() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getChannelId();
        }
        return null;
    }
    
    public String getChannelName() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getChannelName();
        }
        return null;
    }
    
    public Long getDuration() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getDuration();
        }
        return null;
    }
    
    public Long getViewCount() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getViewCount();
        }
        return null;
    }
    
    public Long getLikeCount() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getLikeCount();
        }
        return null;
    }
    
    public Long getTimestamp() {
        if (items != null && !items.isEmpty()) {
            return items.get(0).getTimestamp();
        }
        return null;
    }
    
    @Data
    public static class Item {
        private String id;
        private Snippet snippet;
        private ContentDetails contentDetails;
        private Statistics statistics;
        
        // Getters for the nested data
        public String getTitle() {
            return snippet != null ? snippet.getTitle() : null;
        }
        
        public String getDescription() {
            return snippet != null ? snippet.getDescription() : null;
        }
        
        public String getChannelId() {
            return snippet != null ? snippet.getChannelId() : null;
        }
        
        public String getChannelName() {
            return snippet != null ? snippet.getChannelTitle() : null;
        }
        
        public String getThumbnail() {
            if (snippet != null && snippet.getThumbnails() != null) {
                // Try to get maxres thumbnail first (highest quality)
                if (snippet.getThumbnails().getMaxres() != null && snippet.getThumbnails().getMaxres().getUrl() != null) {
                    return snippet.getThumbnails().getMaxres().getUrl();
                }
                // Try high quality thumbnail
                if (snippet.getThumbnails().getHigh() != null && snippet.getThumbnails().getHigh().getUrl() != null) {
                    return snippet.getThumbnails().getHigh().getUrl();
                }
                // Try medium quality thumbnail
                if (snippet.getThumbnails().getMedium() != null && snippet.getThumbnails().getMedium().getUrl() != null) {
                    return snippet.getThumbnails().getMedium().getUrl();
                }
                // Try default quality thumbnail
                if (snippet.getThumbnails().getDefaultThumbnail() != null && snippet.getThumbnails().getDefaultThumbnail().getUrl() != null) {
                    return snippet.getThumbnails().getDefaultThumbnail().getUrl();
                }
            }
            return null;
        }
        
        public Long getDuration() {
            if (contentDetails != null && contentDetails.getDuration() != null) {
                // Convert ISO 8601 duration to seconds (simplified)
                String duration = contentDetails.getDuration();
                return parseDuration(duration);
            }
            return null;
        }
        
        public Long getViewCount() {
            if (statistics != null && statistics.getViewCount() != null) {
                try {
                    return Long.parseLong(statistics.getViewCount());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
        
        public Long getLikeCount() {
            if (statistics != null && statistics.getLikeCount() != null) {
                try {
                    return Long.parseLong(statistics.getLikeCount());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
        
        public Long getTimestamp() {
            if (snippet != null && snippet.getPublishedAt() != null) {
                try {
                    // Parse ISO 8601 timestamp
                    return Instant.parse(snippet.getPublishedAt()).getEpochSecond();
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }
        
        private Long parseDuration(String duration) {
            if (duration == null) return null;
            
            // Handle format like "PT1H3M6S"
            try {
                // This is a simplified parsing - you might want to use a proper ISO 8601 parser
                // For now, we'll extract hours, minutes, seconds manually
                long totalSeconds = 0;
                if (duration.startsWith("PT")) {
                    duration = duration.substring(2); // Remove "PT"
                    
                    // Parse hours
                    int hIndex = duration.indexOf('H');
                    if (hIndex > 0) {
                        totalSeconds += Long.parseLong(duration.substring(0, hIndex)) * 3600;
                        duration = duration.substring(hIndex + 1);
                    }
                    
                    // Parse minutes
                    int mIndex = duration.indexOf('M');
                    if (mIndex > 0) {
                        totalSeconds += Long.parseLong(duration.substring(0, mIndex)) * 60;
                        duration = duration.substring(mIndex + 1);
                    }
                    
                    // Parse seconds
                    int sIndex = duration.indexOf('S');
                    if (sIndex > 0) {
                        totalSeconds += Long.parseLong(duration.substring(0, sIndex));
                    }
                }
                return totalSeconds;
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Data
    public static class Snippet {
        private String publishedAt;
        private String channelId;
        private String title;
        private String description;
        private Thumbnails thumbnails;
        private String channelTitle;
    }
    
    @Data
    public static class Thumbnails {
        private Thumbnail defaultThumbnail;
        private Thumbnail medium;
        private Thumbnail high;
        private Thumbnail standard;
        private Thumbnail maxres;
        private Thumbnail fhd;
    }
    
    @Data
    public static class Thumbnail {
        private String url;
    }
    
    @Data
    public static class ContentDetails {
        private String duration;
    }
    
    @Data
    public static class Statistics {
        private String viewCount;
        private String likeCount;
    }
    
    @Data
    public static class PageInfo {
        private int totalResults;
        private int resultsPerPage;
    }
}
