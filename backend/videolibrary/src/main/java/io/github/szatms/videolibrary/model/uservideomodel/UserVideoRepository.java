package io.github.szatms.videolibrary.model.uservideomodel;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserVideoRepository extends MongoRepository<UserVideo, String> {
    Optional<UserVideo> findByUserIdAndVideoId(String userId, String videoId);
}
