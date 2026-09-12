package io.github.szatms.videolibrary.model.trashmodel.deletedplaylistitem;

import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItem;
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
@Document(collection = "deleted_user_playlist_items")
public class DeletedUserPlaylistItem {
    @Id
    private String id;

    @Field("user_playlist_item")
    private UserPlaylistItem userPlaylistItem;

    @Field("restore_id")
    private String restoreId;

    @Field("deleted_at")
    private Instant deletedAt;

    @Field("purge_at")
    private Instant purgeAt;
}
