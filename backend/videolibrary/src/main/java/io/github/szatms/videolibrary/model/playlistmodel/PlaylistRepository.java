package io.github.szatms.videolibrary.model.playlistmodel;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends MongoRepository<Playlist, String> {
    Optional<Playlist> findByYoutubeId(String youtubeId);
    boolean existsByYoutubeId(String youtubeId);
    List<Playlist> findByPlaylistIdIn(Collection<String> playlistIds);
}
