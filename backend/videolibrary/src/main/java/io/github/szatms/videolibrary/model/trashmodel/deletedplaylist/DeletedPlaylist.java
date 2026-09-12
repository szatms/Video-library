package io.github.szatms.videolibrary.model.trashmodel.deletedplaylist;

import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deleted_playlists")
public class DeletedPlaylist {
    @Id
    private String id;

    @Field("playlist")
    private Playlist playlist;

    @Field("restore_id")
    private String restoreId;

    @Field("deleted_at")
    private Instant deletedAt;

    @Field("purge_at")
    private Instant purgeAt;
}