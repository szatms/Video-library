package io.github.szatms.videolibrary.model.videomodel;

import io.github.szatms.videolibrary.model.channelmodel.Channel;
import io.github.szatms.videolibrary.model.statsmodel.Stats;
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
@Document(collection = "videos")
public class Video {
    @Id
    private String videoId;

    private String title;
    private String description;

    private Channel channel;
    private Stats stats;

    private Instant publishedAt;
}
