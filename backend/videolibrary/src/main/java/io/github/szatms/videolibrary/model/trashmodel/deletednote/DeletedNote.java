package io.github.szatms.videolibrary.model.trashmodel.deletednote;

import io.github.szatms.videolibrary.model.notemodel.Note;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deleted_notes")
public class DeletedNote {
    @Id
    private String id;

    private Note note;
    private String restoreId;
    private Instant deletedAt;
    private Instant purgeAt;
    private List<String> belongsTo;
}
