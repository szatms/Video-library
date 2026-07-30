package io.github.szatms.videolibrary.model.playlistitemmodel;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistItemRepository extends MongoRepository<PlaylistItem, String> {
    Optional<PlaylistItem> findByVideoId(String videoId);
    Optional<PlaylistItem> findByPlaylistId(String playlistId);

    boolean existsByVideoId(String videoId);
    boolean existsByPlaylistId(String playlistId);

    List<PlaylistItem> findByVideoIdIn(String videoIds);
    List<PlaylistItem> findByPlaylistIdIn(String playlistIds);
}
