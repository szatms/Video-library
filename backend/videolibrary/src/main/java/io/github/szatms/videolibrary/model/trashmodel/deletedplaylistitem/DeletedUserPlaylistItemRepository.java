package io.github.szatms.videolibrary.model.trashmodel.deletedplaylistitem;

import io.github.szatms.videolibrary.model.trashmodel.deletedplaylistitem.DeletedUserPlaylistItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeletedUserPlaylistItemRepository extends MongoRepository<DeletedUserPlaylistItem, String> {
    Optional<DeletedUserPlaylistItem> findByRestoreId(String restoreId);
    List<DeletedUserPlaylistItem> findAllByRestoreId(String restoreId);
    void deleteByRestoreId(String restoreId);
    
    @Query("{'purgeAt': {$lt: ?0}}")
    void deleteAllByPurgeAtBefore(Instant purgeAt);
}
