package io.github.szatms.videolibrary.model.trashmodel.deletedcodex;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeletedCodexRepository extends MongoRepository<DeletedCodex, String> {
    Optional<DeletedCodex> findByRestoreId(String restoreId);
    List<DeletedCodex> findAllByCodexUserId(String userId);

    void deleteByRestoreId(String restoreId);
    
    @Query("{'purgeAt': {$lt: ?0}}")
    void deleteAllByPurgeAtBefore(Instant purgeAt);
}
