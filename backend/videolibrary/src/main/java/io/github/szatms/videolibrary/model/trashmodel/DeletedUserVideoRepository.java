package io.github.szatms.videolibrary.model.trashmodel;

import io.github.szatms.videolibrary.model.trashmodel.DeletedUserVideo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DeletedUserVideoRepository extends MongoRepository<DeletedUserVideo, String> {
    Optional<DeletedUserVideo> findById(String id);
    boolean existsById(String id);
    List<DeletedUserVideo> findAll();

    Optional<DeletedUserVideo> findByRestoreId(String restoreId);

    List<DeletedUserVideo> findAllByUserVideoUserId(String userId);
}
