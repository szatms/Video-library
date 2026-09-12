package io.github.szatms.videolibrary.model.userplaylistitemmodel;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserPlaylistItemRepository extends MongoRepository<UserPlaylistItem, String> {
    Optional<UserPlaylistItem> findByUserVideoId(String userVideoId);
    Optional<UserPlaylistItem> findByUserPlaylistId(String userPlaylistId);

    boolean existsByUserVideoId(String userVideoId);
    boolean existsByUserPlaylistId(String userPlaylistId);

    List<UserPlaylistItem> findByUserVideoIdIn(List<String> userVideoIds);
    List<UserPlaylistItem> findByUserPlaylistIdIn(List<String> userPlaylistIds);
    long countByPlaylistItemId(String playlistItemId);
}
