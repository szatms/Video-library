package io.github.szatms.videolibrary.model.trashmodel.deletedplaylistitem;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeletedPlaylistItemRepository extends MongoRepository<DeletedPlaylistItem, String> {
    Optional<DeletedPlaylistItem> findByRestoreId(String restoreId);
    List<DeletedPlaylistItem> findAllByRestoreId(String restoreId);
    void deleteByRestoreId(String restoreId);
    
    @Query("{'purgeAt': {$lt: ?0}}")
    void deleteAllByPurgeAtBefore(Instant purgeAt);
}
