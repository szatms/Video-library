package io.github.szatms.videolibrary.model.trashmodel.deletedplaylist;

import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedUserPlaylist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeletedUserPlaylistRepository extends MongoRepository<DeletedUserPlaylist, String> {
    boolean existsByRestoreId(String restoreId);
    Optional<DeletedUserPlaylist> findByRestoreId(String restoreId);

    List<DeletedUserPlaylist> findAllByUserPlaylistUserId(String userId);
    List<DeletedUserPlaylist> findAllByRestoreId(String restoreId);
    void deleteByRestoreId(String restoreId);
    
    @Query("{'purgeAt': {$lt: ?0}}")
    void deleteAllByPurgeAtBefore(Instant purgeAt);
}