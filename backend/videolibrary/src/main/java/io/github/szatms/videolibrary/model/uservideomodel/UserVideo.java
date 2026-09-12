package io.github.szatms.videolibrary.model.uservideomodel;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "uservideos")
public class UserVideo {
    @Id
    private String id;

    private String userId;
    private String videoId;

    private List<String> noteIds;
    private boolean watched;
    private List<Timestamp> timestamps;
    private boolean inPlaylist;

    Instant addedAt;
}
