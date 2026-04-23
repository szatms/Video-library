package io.github.szatms.videolibrary.model.videomodel;

import io.github.szatms.videolibrary.model.statsmodel.Stats;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "videos")
public class Video {
    @Id
    private String videoId;

    @Indexed(unique = true)
    private String youtubeId;

    private String title;
    private String description;
    private String thumbnailUrl;

    private String channelId;
    private Stats stats;

    private Instant publishedAt;
}
