package io.github.szatms.videolibrary.integration.youtube.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PythonChannelResponseDTO {
    private List<Item> items;

    @Data
    public static class Item {
        private String id;
        private Snippet snippet;
        private Statistics statistics;
    }

    @Data
    public static class Snippet {
        private String title;
        private String description;
        private String customUrl;
        private Thumbnails thumbnails;
    }

    @Data
    public static class Thumbnails {
        private Thumbnail high;
    }

    @Data
    public static class Thumbnail {
        private String url;
    }

    @Data
    public static class Statistics {
        private Long viewCount;
        private Long subscriberCount;
        private Long videoCount;
    }
}
