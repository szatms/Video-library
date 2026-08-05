package io.github.szatms.videolibrary.model.trashmodel.deletedplaylist;

import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deleted_user_playlists")
public class DeletedUserPlaylist {
    @Id
    private String id;

    @Field("user_playlist")
    private UserPlaylist userPlaylist;

    @Field("restore_id")
    private String restoreId;

    @Field("deleted_at")
    private Instant deletedAt;

    @Field("purge_at")
    private Instant purgeAt;
}