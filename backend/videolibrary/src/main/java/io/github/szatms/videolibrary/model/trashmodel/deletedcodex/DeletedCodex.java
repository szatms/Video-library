package io.github.szatms.videolibrary.model.trashmodel.deletedcodex;

import io.github.szatms.videolibrary.model.codexmodel.Codex;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deleted_codices")
public class DeletedCodex {
    @Id
    private String id;
    private Codex codex;
    private String restoreId;
    private Instant deletedAt;
    private Instant purgeAt;
}
