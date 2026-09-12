package io.github.szatms.videolibrary.model.trashmodel.deletedplaylist;

import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylist;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
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

    private List<String> belongsTo;
}