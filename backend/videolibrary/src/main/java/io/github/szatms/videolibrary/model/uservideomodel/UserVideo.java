package io.github.szatms.videolibrary.model.uservideomodel;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "uservideos")
@CompoundIndex(def = "{'userId': 1, 'videoId': 1}", unique = true)
public class UserVideo {
    @Id
    private String id;

    private String userId;
    private String videoId;

    private String note;
    private boolean watched;

    Instant addedAt;
}
