package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.NoteMapper;
import io.github.szatms.videolibrary.mapper.UserVideoMapper;
import io.github.szatms.videolibrary.mapper.VideoMapper;
import io.github.szatms.videolibrary.model.notemodel.Note;
import io.github.szatms.videolibrary.model.notemodel.NoteRepository;
import io.github.szatms.videolibrary.model.notemodel.dto.AddToParentDTO;
import io.github.szatms.videolibrary.model.notemodel.dto.NoteResponseDTO;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItem;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylist;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylistRepository;
import io.github.szatms.videolibrary.model.uservideomodel.SortDirection;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoSortBy;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoCreateDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoResponseDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoSummaryResponseDTO;
import io.github.szatms.videolibrary.model.uservideomodel.dto.UserVideoUpdateDTO;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.service.NoteService;
import io.github.szatms.videolibrary.service.UserPlaylistItemService;
import io.github.szatms.videolibrary.service.UserPlaylistService;
import io.github.szatms.videolibrary.service.UserVideoService;
import io.github.szatms.videolibrary.service.VideoService;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.utils.LinkUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uservideos")
public class UserVideoController {
    private final UserVideoService userVideoService;
    private final UserVideoMapper userVideoMapper;
    private final VideoService videoService;
    private final VideoMapper videoMapper;
    private final LinkUtils linkUtils;
    private final UserPlaylistService userPlaylistService;
    private final UserPlaylistItemService userPlaylistItemService;
    private final NoteService noteService;
    private final NoteMapper noteMapper;

    @GetMapping
    public List<UserVideoSummaryResponseDTO> getVideos(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "ADDED_AT") UserVideoSortBy sortBy,
            @RequestParam(defaultValue = "DESC") SortDirection direction,
            @RequestParam(defaultValue = "true") boolean unwatchedFirst
    ) {
        String userId = userDetails.getUser().getUserId();
        List<UserVideo> userVideos = userVideoService.getVideos(userId, sortBy, direction, unwatchedFirst);
        Map<String, Video> videosById = getVideosById(userVideos);

        return userVideos.stream()
                .map(userVideo -> userVideoMapper.toSummaryResponseDTO(
                        userVideo,
                        videoMapper.toSummaryDTO(requireVideo(videosById, userVideo.getVideoId()))
                ))
                .toList();
    }

    @GetMapping("/detailed")
    public List<UserVideoResponseDTO> getDetailedVideos(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        List<UserVideo> userVideos = userVideoService.getVideos(
                userId,
                UserVideoSortBy.ADDED_AT,
                SortDirection.DESC,
                true
        );
        Map<String, Video> videosById = getVideosById(userVideos);

        return userVideos.stream()
                .map(userVideo -> userVideoMapper.toResponseDTO(
                        userVideo,
                        videoMapper.toResponseDTO(requireVideo(videosById, userVideo.getVideoId()))
                ))
                .toList();
    }

    @PostMapping
    public UserVideoResponseDTO addVideo(
            @RequestBody UserVideoCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getUser().getUserId();
        String youtubeId = linkUtils.getYTVideoId(dto.getUrl());

        if (youtubeId == null) {
            throw new IllegalArgumentException("Invalid YouTube URL");
        }

        var userVideo = userVideoService.addVideo(userId, youtubeId);

        Video video = videoService.getById(userVideo.getVideoId());

        return userVideoMapper.toResponseDTO(
                userVideo,
                videoMapper.toResponseDTO(video)
        );
    }

    @GetMapping("/{id}")
    public UserVideoResponseDTO getVideo(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        UserVideo uv = userVideoService.getVideo(userId, id);

        Video video = videoService.getById(uv.getVideoId());

        return userVideoMapper.toResponseDTO(
                uv,
                videoMapper.toResponseDTO(video)
        );
    }

    @PatchMapping("/{id}")
    public UserVideoResponseDTO updateVideo(
            @PathVariable String id,
            @RequestBody UserVideoUpdateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        UserVideo userVideo = userVideoService.updateVideo(userId, id, dto);
        Video video = videoService.getById(userVideo.getVideoId());

        return userVideoMapper.toResponseDTO(
                userVideo,
                videoMapper.toResponseDTO(video)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        userVideoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/playlist/{id}")
    public List<UserVideoSummaryResponseDTO> getAllForPlaylist(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        List<UserPlaylistItem> items = userPlaylistItemService.getAllByPlaylistId(id);

        List<UserVideoSummaryResponseDTO> results = new ArrayList<>();

        for (UserPlaylistItem item : items) {
            String userVideoId = item.getUserVideoId();
            UserVideo userVideo = userVideoService.getVideo(userId, userVideoId);
            Video video = videoService.getById(userVideo.getVideoId());
            results.add(userVideoMapper.toSummaryResponseDTO(userVideo, videoMapper.toSummaryDTO(video)));
        }

        return results;
    }

    private Map<String, Video> getVideosById(List<UserVideo> userVideos) {
        return videoService.getByIds(userVideos.stream()
                        .map(UserVideo::getVideoId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(Video::getVideoId, Function.identity()));
    }

    private Video requireVideo(Map<String, Video> videosById, String videoId) {
        Video video = videosById.get(videoId);
        if (video == null) {
            throw new IllegalStateException("Video not found");
        }
        return video;
    }

    @GetMapping("/{id}/notes")
    public List<NoteResponseDTO> getNotes(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        
        // Verify that the video belongs to the authenticated user
        userVideoService.getVideo(userId, id);
        
        List<Note> notes = noteService.getNotesForParent(userId, id);

        return notes.stream()
                .map(noteMapper::toResponseDTO)
                .toList();
    }

    @PostMapping("/{id}/notes/add")
    public ResponseEntity<Void> addToParent(
            @PathVariable String id,
            @RequestBody AddToParentDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        
        // Verify that the video belongs to the authenticated user
        userVideoService.getVideo(userId, id);
        
        // Add notes to the parent video
        noteService.addToParent(userId, dto);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/notes/remove")
    public ResponseEntity<Void> removeFromParent(
            @PathVariable String id,
            @RequestBody AddToParentDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        
        // Verify that the video belongs to the authenticated user
        userVideoService.getVideo(userId, id);
        
        // Remove notes from the parent video
        noteService.removeFromParent(userId, dto);
        
        return ResponseEntity.ok().build();
    }
}
