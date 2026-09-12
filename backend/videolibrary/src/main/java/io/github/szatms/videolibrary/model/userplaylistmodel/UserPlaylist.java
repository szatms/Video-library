package io.github.szatms.videolibrary.model.userplaylistmodel;

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
@Document(collection = "userplaylists")
public class UserPlaylist {
    @Id
    private String id;

    private String userId;
    private String playlistId;

    private List<String> noteIds;
    private boolean watched;
    private List<String> items;

    Instant addedAt;
}
