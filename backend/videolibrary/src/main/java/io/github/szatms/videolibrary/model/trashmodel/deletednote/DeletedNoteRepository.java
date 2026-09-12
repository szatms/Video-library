package io.github.szatms.videolibrary.model.trashmodel.deletednote;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeletedNoteRepository extends MongoRepository<DeletedNote, String> {
    Optional<DeletedNote> findByRestoreId(String restoreId);
    List<DeletedNote> findAllByNoteUserId(String userId);

    void deleteByRestoreId(String restoreId);
    
    @Query("{'purgeAt': {$lt: ?0}}")
    void deleteAllByPurgeAtBefore(Instant purgeAt);
}
