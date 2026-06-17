package io.github.szatms.videolibrary.model.channelmodel;

import io.github.szatms.videolibrary.model.statsmodel.ChannelStats;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "channels")
public class Channel {
    @Id
    private String channelId;
    private String channelTitle;
    private String channelDescription;
    private String channelHandle;
    private String channelThumbnail;

    private ChannelStats channelStats;
}
