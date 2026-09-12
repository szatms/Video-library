package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.exception.PythonMicroserviceUnavailableException;
import io.github.szatms.videolibrary.integration.youtube.PythonPlaylistDataProvider;
import io.github.szatms.videolibrary.integration.youtube.factory.PlaylistFactory;
import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.playlistmodel.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final PlaylistFactory playlistFactory;
    private final PythonPlaylistDataProvider provider;
    private final ChannelService channelService;

    public Playlist getOrCreatePlaylist(String youtubeId) {
        return playlistRepository.findByYoutubeId(youtubeId)
                .orElseGet(() -> {
                    try {
                        var response = provider.load(youtubeId);
                        if (response == null || response.getId() == null || response.getId().isBlank()){
                            throw new IllegalArgumentException("No playlist found for youtubeId: " + youtubeId);
                        }
                        // Only fetch channel if we have a valid channel ID
                        String channelId = response.getChannelId();
                        if (channelId != null && !channelId.isBlank()) {
                            channelService.getOrCreateChannel(channelId);
                        } else {
                            // Log that we couldn't get channel ID from playlist
                            System.out.println("Warning: No channel ID found in playlist response for youtubeId: " + youtubeId);
                        }

                        Playlist playlist = playlistFactory.fromItem(response);
                        playlist.setYoutubeId(youtubeId);
                        return playlistRepository.save(playlist);
                    } catch (ResourceAccessException e) {
                        throw new PythonMicroserviceUnavailableException("Playlist parsing down", e);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to fetch playlist for youtubeId: " + youtubeId, e);
                    }
                });
    }

    public Playlist getById(String playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalStateException("Video not found."));
    }

    public List<Playlist> getByIds(Iterable<String> playlistIds) {return playlistRepository.findAllById(playlistIds);}

    public List<Playlist> getPlaylistsByChannel(String channelId) {
        return playlistRepository.findByChannelId(channelId);
    }

    public void deleteById(String playlistId) {playlistRepository.deleteById(playlistId);}
}
