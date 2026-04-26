package io.github.szatms.videolibrary.integration.youtube.dto;

import lombok.Data;

import java.util.List;

@Data
public class PythonVideoResponseDTO {
    List<Item> items;

    @Data
    public static class Item {
        String id;
        Snippet snippet;
        Statistics statistics;
    }

    @Data
    public static class Snippet {
        String title;
        String description;
        String channelId;
        String publishedAt;
        Thumbnails thumbnails;
    }

    @Data
    public static class Thumbnails {private Thumbnail high;}

    @Data
    public static class Thumbnail {private String url;}

    @Data
    public static class Statistics {
        String viewCount;
        String likeCount;
    }
}
