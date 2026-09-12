package io.github.szatms.videolibrary.model.trashmodel.deletedvideo;

import io.github.szatms.videolibrary.model.videomodel.Video;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "deleted_videos")
public class DeletedVideo {
    @Id
    private String id;
    private String restoreId;
    private Instant deletedAt;
    private Instant purgeAt;
    private Video video;
}
