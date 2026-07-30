package io.github.szatms.videolibrary.model.playlistmodel;

import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "playlists")
public class Playlist {
    @Id
    private String id;
    private String youtubeId;
    private String channelId;

    private String title;
    private String description;
    private String thumbnailUrl;
    private Integer videoCount;
    private List<PlaylistItem> items;
}
