package io.github.szatms.videolibrary.model.playlistitemmodel;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "playlistitems")
public class PlaylistItem {
    @Id
    private String id;
    private String playlistId;
    private String videoId;
    private int position;
}
