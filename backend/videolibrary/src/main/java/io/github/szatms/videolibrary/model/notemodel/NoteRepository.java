package io.github.szatms.videolibrary.model.notemodel;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends MongoRepository<Note, String> {
    Optional<Note> findByParentIdsContaining(String parentId);
    boolean existsByParentIdsContaining(String parentId);

    long countByParentIdsContaining(String parentId);
    List<Note> findByUserId(String userId);
    
    @Query("{'parentIds': {$elemMatch: {$in: ?0}}}")
    List<Note> findByParentIdsInList(List<String> parentIds);
}
