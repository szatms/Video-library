package io.github.szatms.videolibrary.model.notemodel;

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
@Document(collection = "notes")
public class Note {
    @Id
    private String id;
    private String userId;
    private List<String> parentIds;

    private String title;
    private String content;

    private Boolean isPdf;
    private String pdfUrl;

    private Instant addedAt;
    private Instant updatedAt;
}
