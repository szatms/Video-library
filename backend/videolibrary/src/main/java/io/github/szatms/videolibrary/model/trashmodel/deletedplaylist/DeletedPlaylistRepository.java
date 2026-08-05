package io.github.szatms.videolibrary.model.trashmodel.deletedplaylist;

import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedPlaylist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedPlaylistRepository extends MongoRepository<DeletedPlaylist, String> {
    boolean existsByRestoreId(String restoreId);
    DeletedPlaylist findByRestoreId(String restoreId);
}