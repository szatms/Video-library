package io.github.szatms.videolibrary.model.trashmodel.deletedvideo;

import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "deleted_user_videos")
public class DeletedUserVideo {
    @Id
    private String id;
    private String restoreId;
    private Instant deletedAt;
    private Instant purgeAt;
    private UserVideo userVideo;
    private List<String> belongsTo;
}
