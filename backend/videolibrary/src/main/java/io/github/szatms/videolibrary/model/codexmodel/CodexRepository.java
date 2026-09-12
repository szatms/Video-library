package io.github.szatms.videolibrary.model.codexmodel;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CodexRepository extends MongoRepository<Codex, String> {
    Optional<Codex> findByUserIdAndId(String userId, String Id);
    List<Codex> findAllByUserId(String userId);
    Codex getById(String id);
    List<Codex> findAllByContentIds(String contentIds);
    List<Codex> findAllById(List<String> Id);
}
