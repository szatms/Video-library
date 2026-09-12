package io.github.szatms.videolibrary.model.userplaylistmodel;

import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserPlaylistRepository extends MongoRepository<UserPlaylist, String> {
    Optional<UserPlaylist> findByUserIdAndPlaylistId(String userId, String playlistId);
    List<UserPlaylist> findAllByUserId(String userId, Sort sort);
    long countByPlaylistId(String playlistId);
    
    @Query("{'userId': ?0, 'playlistId': {$in: ?1}}")
    List<UserPlaylist> findByUserIdAndPlaylistIds(String userId, List<String> playlistIds);
}
