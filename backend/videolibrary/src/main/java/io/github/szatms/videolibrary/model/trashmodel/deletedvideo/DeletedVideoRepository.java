package io.github.szatms.videolibrary.model.trashmodel.deletedvideo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeletedVideoRepository extends MongoRepository<DeletedVideo, String> {
    Optional<DeletedVideo> findById(String id);
    boolean existsById(String id);
    List<DeletedVideo> findAll();

    Optional<DeletedVideo> findByRestoreId(String restoreId);
    boolean existsByRestoreId(String restoreId);

    List<DeletedVideo> findAllByRestoreId(String restoreId);

    void deleteByRestoreId(String restoreId);
    
    @Query("{'purgeAt': {$lt: ?0}}")
    void deleteAllByPurgeAtBefore(Instant purgeAt);
}
