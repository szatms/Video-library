package io.github.szatms.videolibrary.model.trashmodel.deletedplaylist;

import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedUserPlaylist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedUserPlaylistRepository extends MongoRepository<DeletedUserPlaylist, String> {
    boolean existsByRestoreId(String restoreId);
    DeletedUserPlaylist findByRestoreId(String restoreId);
}