package io.github.szatms.videolibrary.model.trashmodel;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DeletedVideoRepository extends MongoRepository<DeletedVideo, String> {
    Optional<DeletedVideo> findById(String id);
    boolean existsById(String id);
    List<DeletedVideo> findAll();

    Optional<DeletedVideo> findByRestoreId(String restoreId);
    boolean existsByRestoreId(String restoreId);
}
