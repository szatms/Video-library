package io.github.szatms.videolibrary.model.videomodel;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VideoRepository extends MongoRepository<Video, String> {
    Optional<Video> findByVideoId(String videoId);
    boolean existsByVideoId(String videoId);
}
