package io.github.szatms.videolibrary.model.userplaylistitemmodel;

import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "userplaylistitems")
public class UserPlaylistItem {
    @Id
    private String id;
    private String userPlaylistId;
    private String userVideoId;
    private PlaylistItem playlistItem;
}
