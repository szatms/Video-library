package io.github.szatms.videolibrary.model.trashmodel.deletedplaylist;

import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedPlaylist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeletedPlaylistRepository extends MongoRepository<DeletedPlaylist, String> {
    boolean existsByRestoreId(String restoreId);
    Optional<DeletedPlaylist> findByRestoreId(String restoreId);
    List<DeletedPlaylist> findAllByRestoreId(String restoreId);
    void deleteByRestoreId(String restoreId);
    
    @Query("{'purgeAt': {$lt: ?0}}")
    void deleteAllByPurgeAtBefore(Instant purgeAt);
}