package io.github.szatms.videolibrary.model.codexmodel;

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
@Document(collection = "codices")
public class Codex {
    @Id
    private String id;
    private String userId;
    private String title;
    private String description;
    private List<String> contentIds;
    private Instant addedAt;
    private Instant updatedAt;
}
