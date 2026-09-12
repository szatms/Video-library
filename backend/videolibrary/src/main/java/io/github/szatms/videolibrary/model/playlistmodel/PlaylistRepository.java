package io.github.szatms.videolibrary.model.playlistmodel;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends MongoRepository<Playlist, String> {
    Optional<Playlist> findByYoutubeId(String youtubeId);
    boolean existsByYoutubeId(String youtubeId);
    List<Playlist> findByYoutubeIdIn(Collection<String> youtubeIds);
    @Query("{'channelId': ?0}")
    List<Playlist> findByChannelId(String channelId);
    Playlist getById(String playlistId);
}
