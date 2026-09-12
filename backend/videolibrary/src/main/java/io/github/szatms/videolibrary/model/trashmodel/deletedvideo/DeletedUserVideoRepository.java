package io.github.szatms.videolibrary.model.trashmodel.deletedvideo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeletedUserVideoRepository extends MongoRepository<DeletedUserVideo, String> {
    Optional<DeletedUserVideo> findById(String id);
    boolean existsById(String id);
    List<DeletedUserVideo> findAll();

    Optional<DeletedUserVideo> findByRestoreId(String restoreId);

    List<DeletedUserVideo> findAllByUserVideoUserId(String userId);

    List<DeletedUserVideo> findAllByRestoreId(String restoreId);

    void deleteByRestoreId(String restoreId);
    
    @Query("{'purgeAt': {$lt: ?0}}")
    void deleteAllByPurgeAtBefore(Instant purgeAt);
}
