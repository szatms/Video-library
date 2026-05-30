package io.github.szatms.videolibrary.model.uservideomodel;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserVideoRepository extends MongoRepository<UserVideo, String> {
    Optional<UserVideo> findByUserIdAndVideoId(String userId, String videoId);
    List<UserVideo> findAllByUserId(String userId, Sort sort);
    long countByVideoId(String videoId);
}
