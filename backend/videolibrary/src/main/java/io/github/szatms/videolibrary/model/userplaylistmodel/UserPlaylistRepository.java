package io.github.szatms.videolibrary.model.userplaylistmodel;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserPlaylistRepository extends MongoRepository<UserPlaylist, String> {
    Optional<UserPlaylist> findByUserIdAndPlaylistId(String userId, String playlistId);
    List<UserPlaylist> findAllByUserId(String userId, Sort sort);
    long countByPlaylistId(String playlistId);
}
