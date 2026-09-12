package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.*;
import io.github.szatms.videolibrary.model.codexmodel.Codex;
import io.github.szatms.videolibrary.model.codexmodel.CodexRepository;
import io.github.szatms.videolibrary.model.notemodel.Note;
import io.github.szatms.videolibrary.model.notemodel.NoteRepository;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItem;
import io.github.szatms.videolibrary.model.playlistitemmodel.PlaylistItemRepository;
import io.github.szatms.videolibrary.model.playlistmodel.Playlist;
import io.github.szatms.videolibrary.model.playlistmodel.PlaylistRepository;
import io.github.szatms.videolibrary.model.trashmodel.deletedcodex.DeletedCodex;
import io.github.szatms.videolibrary.model.trashmodel.deletedcodex.DeletedCodexRepository;
import io.github.szatms.videolibrary.model.trashmodel.deletednote.DeletedNote;
import io.github.szatms.videolibrary.model.trashmodel.deletednote.DeletedNoteRepository;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedPlaylist;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedPlaylistRepository;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedUserPlaylist;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylist.DeletedUserPlaylistRepository;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylistitem.DeletedPlaylistItem;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylistitem.DeletedPlaylistItemRepository;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylistitem.DeletedUserPlaylistItem;
import io.github.szatms.videolibrary.model.trashmodel.deletedplaylistitem.DeletedUserPlaylistItemRepository;
import io.github.szatms.videolibrary.model.trashmodel.deletedvideo.DeletedUserVideo;
import io.github.szatms.videolibrary.model.trashmodel.deletedvideo.DeletedUserVideoRepository;
import io.github.szatms.videolibrary.model.trashmodel.deletedvideo.DeletedVideo;
import io.github.szatms.videolibrary.model.trashmodel.deletedvideo.DeletedVideoRepository;
import io.github.szatms.videolibrary.model.trashmodel.dto.TrashItemDisplayDTO;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItem;
import io.github.szatms.videolibrary.model.userplaylistitemmodel.UserPlaylistItemRepository;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylist;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylistRepository;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoRepository;
import io.github.szatms.videolibrary.model.videomodel.Video;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import io.github.szatms.videolibrary.settings.appsettings.AppSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashService {
    private final DeletedVideoRepository deletedVideoRepository;
    private final DeletedUserVideoRepository deletedUserVideoRepository;
    private final DeletedVideoMapper deletedVideoMapper;
    private final DeletedUserVideoMapper deletedUserVideoMapper;
    private final VideoRepository videoRepository;
    private final UserVideoRepository userVideoRepository;
    private final DeletedPlaylistMapper deletedPlaylistMapper;
    private final DeletedPlaylistRepository deletedPlaylistRepository;
    private final DeletedUserPlaylistMapper deletedUserPlaylistMapper;
    private final DeletedUserPlaylistRepository deletedUserPlaylistRepository;
    private final PlaylistRepository playlistRepository;
    private final UserPlaylistRepository userPlaylistRepository;
    private final DeletedPlaylistItemMapper deletedPlaylistItemMapper;
    private final DeletedPlaylistItemRepository deletedPlaylistItemRepository;
    private final DeletedUserPlaylistItemMapper deletedUserPlaylistItemMapper;
    private final DeletedUserPlaylistItemRepository deletedUserPlaylistItemRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final UserPlaylistItemRepository userPlaylistItemRepository;
    private final NoteRepository noteRepository;
    private final DeletedNoteMapper deletedNoteMapper;
    private final DeletedNoteRepository deletedNoteRepository;
    private final CodexRepository codexRepository;
    private final DeletedCodexMapper deletedCodexMapper;
    private final DeletedCodexRepository deletedCodexRepository;
    private final AppSettingsService appSettingsService;

    //=========================
    // MOVING ENTITIES TO TRASH
    //=========================
    public DeletedVideo moveVideoToTrash(Video video, String restoreId) {

        DeletedVideo deletedVideo = deletedVideoMapper.toDeletedVideo(video);

        deletedVideo.setRestoreId(restoreId);
        deletedVideo.setDeletedAt(Instant.now());
        deletedVideo.setPurgeAt(Instant.now().plus(appSettingsService.getAppSettings().getDeletionPeriod(), ChronoUnit.DAYS));

        return deletedVideoRepository.save(deletedVideo);
    }

    public DeletedUserVideo moveUserVideoToTrash(UserVideo userVideo, String restoreId) {
        DeletedUserVideo deletedUserVideo = deletedUserVideoMapper.toDeletedUserVideo(userVideo);

        deletedUserVideo.setRestoreId(restoreId);
        deletedUserVideo.setDeletedAt(Instant.now());
        deletedUserVideo.setPurgeAt(Instant.now().plus(appSettingsService.getAppSettings().getDeletionPeriod(), ChronoUnit.DAYS));
        deletedUserVideo.setBelongsTo(new ArrayList<>());

        List<String> noteIds = userVideo.getNoteIds();
        String parentId = userVideo.getId();

        for(String id : noteIds){
            Note note = noteRepository.findById(id).orElseThrow(RuntimeException::new);
            note.getParentIds().remove(parentId);
            noteRepository.save(note);
        }

        List<Codex> codices = codexRepository.findAllByContentIds(parentId);
        for (Codex codex : codices){
            deletedUserVideo.getBelongsTo().add(codex.getId());
            codex.getContentIds().remove(parentId);
            codexRepository.save(codex);
        }

        return deletedUserVideoRepository.save(deletedUserVideo);
    }

    public DeletedPlaylist movePlaylistToTrash(Playlist playlist, String restoreId) {
        DeletedPlaylist deletedPlaylist = deletedPlaylistMapper.toDeletedPlaylist(playlist);

        deletedPlaylist.setRestoreId(restoreId);
        deletedPlaylist.setDeletedAt(Instant.now());
        deletedPlaylist.setPurgeAt(Instant.now().plus(appSettingsService.getAppSettings().getDeletionPeriod(), ChronoUnit.DAYS));
        
        return deletedPlaylistRepository.save(deletedPlaylist);
    }

    public DeletedUserPlaylist moveUserPlaylistToTrash(UserPlaylist userPlaylist, String restoreId){
        DeletedUserPlaylist deletedUserPlaylist = deletedUserPlaylistMapper.toDeletedUserPlaylist(userPlaylist);

        deletedUserPlaylist.setRestoreId(restoreId);
        deletedUserPlaylist.setDeletedAt(Instant.now());
        deletedUserPlaylist.setPurgeAt(Instant.now().plus(appSettingsService.getAppSettings().getDeletionPeriod(), ChronoUnit.DAYS));
        deletedUserPlaylist.setBelongsTo(new ArrayList<>());

        List<String> noteIds = userPlaylist.getNoteIds();
        String parentId = userPlaylist.getId();

        for(String id : noteIds){
            Note note = noteRepository.findById(id).orElseThrow(RuntimeException::new);
            note.getParentIds().remove(parentId);
            noteRepository.save(note);
        }

        List<Codex> codices = codexRepository.findAllByContentIds(parentId);
        for (Codex codex : codices){
            deletedUserPlaylist.getBelongsTo().add(codex.getId());
            codex.getContentIds().remove(parentId);
            codexRepository.save(codex);
        }
        
        return deletedUserPlaylistRepository.save(deletedUserPlaylist);
    }

    public DeletedPlaylistItem movePlaylistItemToTrash(PlaylistItem playlistItem, String restoreId){
        DeletedPlaylistItem deletedPlaylistItem = deletedPlaylistItemMapper.toDeletedPlaylistItem(playlistItem);

        deletedPlaylistItem.setRestoreId(restoreId);
        deletedPlaylistItem.setDeletedAt(Instant.now());
        deletedPlaylistItem.setPurgeAt(Instant.now().plus(appSettingsService.getAppSettings().getDeletionPeriod(), ChronoUnit.DAYS));
        return deletedPlaylistItemRepository.save(deletedPlaylistItem);
    }

    public DeletedUserPlaylistItem moveUserPlaylistItemToTrash(UserPlaylistItem userPlaylistItem, String restoreId){
        DeletedUserPlaylistItem deletedUserPlaylistItem = deletedUserPlaylistItemMapper.toDeletedUserPlaylistItem(userPlaylistItem);

        deletedUserPlaylistItem.setRestoreId(restoreId);
        deletedUserPlaylistItem.setDeletedAt(Instant.now());
        deletedUserPlaylistItem.setPurgeAt(Instant.now().plus(appSettingsService.getAppSettings().getDeletionPeriod(), ChronoUnit.DAYS));

        return deletedUserPlaylistItemRepository.save(deletedUserPlaylistItem);
    }

    public DeletedNote moveNoteToTrash(Note note, String restoreId){
        DeletedNote deletedNote = deletedNoteMapper.toDeletedNote(note);

        deletedNote.setRestoreId(restoreId);
        deletedNote.setDeletedAt(Instant.now());
        deletedNote.setPurgeAt(Instant.now().plus(appSettingsService.getAppSettings().getDeletionPeriod(), ChronoUnit.DAYS));
        deletedNote.setBelongsTo(new ArrayList<>());

        String parentId = note.getId();
        List<Codex> codices = codexRepository.findAllByContentIds(parentId);
        for (Codex codex : codices){
            deletedNote.getBelongsTo().add(codex.getId());
            codex.getContentIds().remove(parentId);
            codexRepository.save(codex);
        }

        noteRepository.delete(note);
        return deletedNoteRepository.save(deletedNote);
    }

    public DeletedCodex moveCodexToTrash(Codex codex, String restoreId){
        DeletedCodex deletedCodex = deletedCodexMapper.toDeletedCodex(codex);

        deletedCodex.setRestoreId(restoreId);
        deletedCodex.setDeletedAt(Instant.now());
        deletedCodex.setPurgeAt(Instant.now().plus(appSettingsService.getAppSettings().getDeletionPeriod(), ChronoUnit.DAYS));

        codexRepository.delete(codex);
        return deletedCodexRepository.save(deletedCodex);
    }

    //=========================
    // RESTORING ENTITIES
    //=========================
    public void restoreVideos(String restoreId) {
        if (deletedVideoRepository.existsByRestoreId(restoreId)){
            DeletedVideo deletedVideo = deletedVideoRepository.findByRestoreId(restoreId).orElseThrow(RuntimeException::new);
            restoreVideo(deletedVideo);
        }
        DeletedUserVideo deletedUserVideo = deletedUserVideoRepository.findByRestoreId(restoreId).orElseThrow(RuntimeException::new);
        restoreUserVideo(deletedUserVideo);
    }

    public void restorePlaylists(String restoreId){
        List<DeletedPlaylistItem> playlistItemsToRestore = deletedPlaylistItemRepository.findAllByRestoreId(restoreId);
        List<DeletedUserPlaylistItem> userPlaylistItemsToRestore = deletedUserPlaylistItemRepository.findAllByRestoreId(restoreId);
        List<DeletedVideo> videosToRestore = deletedVideoRepository.findAllByRestoreId(restoreId);
        List<DeletedUserVideo> userVideosToRestore = deletedUserVideoRepository.findAllByRestoreId(restoreId);

        restorePlaylistItems(playlistItemsToRestore);
        restoreUserPlaylistItems(userPlaylistItemsToRestore);
        restoreVideos(videosToRestore);
        restoreUserVideos(userVideosToRestore);
        restorePlaylist(restoreId);
        restoreUserPlaylist(restoreId);
    }

    private void restoreVideos(List<DeletedVideo> deletedVideos){
        for(DeletedVideo video : deletedVideos){
            restoreVideo(video);
        }
    }

    public void restoreVideo(DeletedVideo deletedVideo) {
            Video restoredVideo = deletedVideoMapper.toVideo(deletedVideo);
            videoRepository.save(restoredVideo);
            deletedVideoRepository.delete(deletedVideo);
    }

    public void restoreUserVideos(List<DeletedUserVideo> deletedUserVideos){
        for (DeletedUserVideo video : deletedUserVideos){
            restoreUserVideo(video);
        }
    }

    public void restoreUserVideo(DeletedUserVideo deletedUserVideo) {
        UserVideo userVideo = deletedUserVideoMapper.toUserVideo(deletedUserVideo);

        List<String> noteIds = userVideo.getNoteIds();
        String parentId = userVideo.getId();
        for(String id : noteIds){
            Note note = noteRepository.findById(id).orElseThrow(RuntimeException::new);
            note.getParentIds().add(parentId);
            noteRepository.save(note);
        }

        List<Codex> attachTo = codexRepository.findAllById(deletedUserVideo.getBelongsTo());
        for (Codex codex : attachTo){
            codex.getContentIds().add(parentId);
            codexRepository.save(codex);
        }

        userVideoRepository.save(userVideo);
        deletedUserVideoRepository.delete(deletedUserVideo);
    }

    public void restorePlaylist(String restoreId) {
        DeletedPlaylist deletedPlaylist = deletedPlaylistRepository
                .findByRestoreId(restoreId)
                .orElseThrow(RuntimeException::new);
        Playlist restoredPlaylist = deletedPlaylistMapper.toPlaylist(deletedPlaylist);
        playlistRepository.save(restoredPlaylist);
        deletedPlaylistRepository.delete(deletedPlaylist);
    }

    public void restoreUserPlaylist(String restoreId) {
        DeletedUserPlaylist deletedUserPlaylist = deletedUserPlaylistRepository
                .findByRestoreId(restoreId)
                .orElseThrow(RuntimeException::new);
        UserPlaylist restoredUserPlaylist = deletedUserPlaylistMapper.toUserPlaylist(deletedUserPlaylist);

        List<String> noteIds = restoredUserPlaylist.getNoteIds();
        String parentId = restoredUserPlaylist.getId();
        for(String id : noteIds){
            Note note = noteRepository.findById(id).orElseThrow(RuntimeException::new);
            note.getParentIds().add(parentId);
            noteRepository.save(note);
        }

        List<Codex> attachTo = codexRepository.findAllById(deletedUserPlaylist.getBelongsTo());
        for (Codex codex : attachTo){
            codex.getContentIds().add(parentId);
            codexRepository.save(codex);
        }
        
        userPlaylistRepository.save(restoredUserPlaylist);
        deletedUserPlaylistRepository.delete(deletedUserPlaylist);
    }

    public void restoreNote(String restoreId){
        DeletedNote deletedNote = deletedNoteRepository
                .findByRestoreId(restoreId)
                .orElseThrow(RuntimeException::new);
        Note restoredNote = deletedNoteMapper.toNote(deletedNote);

        String parentId = restoredNote.getId();
        List<Codex> attachTo = codexRepository.findAllById(deletedNote.getBelongsTo());
        for (Codex codex : attachTo){
            codex.getContentIds().add(parentId);
            codexRepository.save(codex);
        }

        noteRepository.save(restoredNote);
        deletedNoteRepository.delete(deletedNote);
    }

    public void restoreCodex(String restoreId){
        DeletedCodex deletedCodex = deletedCodexRepository
                .findByRestoreId(restoreId)
                .orElseThrow(RuntimeException::new);
        Codex restoredCodex = deletedCodexMapper.toCodex(deletedCodex);
        codexRepository.save(restoredCodex);
        deletedCodexRepository.delete(deletedCodex);
    }

    public void restorePlaylistItems(List<DeletedPlaylistItem> deletedPlaylistItems){
        for (DeletedPlaylistItem item : deletedPlaylistItems){
            restorePlaylistItem(item);
        }
    }

    public void restorePlaylistItem(DeletedPlaylistItem deletedPlaylistItem){
        PlaylistItem restoredPlaylistItem = deletedPlaylistItemMapper.toPlaylistItem(deletedPlaylistItem);
        playlistItemRepository.save(restoredPlaylistItem);
        deletedPlaylistItemRepository.delete(deletedPlaylistItem);
    }

    public void restoreUserPlaylistItems(List<DeletedUserPlaylistItem> deletedUserPlaylistItems){
        for (DeletedUserPlaylistItem item : deletedUserPlaylistItems){
            restoreUserPlaylistItem(item);
        }
    }

    public void restoreUserPlaylistItem(DeletedUserPlaylistItem deletedUserPlaylistItem){
        UserPlaylistItem restoredUserPlaylistItem = deletedUserPlaylistItemMapper.toUserPlaylistItem(deletedUserPlaylistItem);
        userPlaylistItemRepository.save(restoredUserPlaylistItem);
        deletedUserPlaylistItemRepository.delete(deletedUserPlaylistItem);
    }

    //=========================
    // GETTERS
    //=========================

    public List<TrashItemDisplayDTO> getDeletedUserVideos(String userId) {

        return deletedUserVideoRepository
                .findAllByUserVideoUserId(userId)
                .stream()
                .map(deletedUserVideo -> {

                    TrashItemDisplayDTO dto = new TrashItemDisplayDTO();

                    UserVideo userVideo = deletedUserVideo.getUserVideo();

                    Video video;
                    // First try to find the video from DeletedVideo
                    List<DeletedVideo> deletedVideos = deletedVideoRepository.findAllByRestoreId(deletedUserVideo.getRestoreId());
                    if (!deletedVideos.isEmpty()) {
                        video = deletedVideos.get(0).getVideo();
                    } else {
                        // Fallback to finding video directly by ID from UserVideo
                        video = videoRepository
                                .findById(userVideo.getVideoId())
                                .orElseThrow(() -> new RuntimeException("Video not found for deleted user video with ID: " + deletedUserVideo.getId()));
                    }

                    dto.setId(deletedUserVideo.getId());
                    dto.setRestoreId(deletedUserVideo.getRestoreId());
                    dto.setInPlaylist(userVideo.isInPlaylist());
                    dto.setAddedAt(userVideo.getAddedAt());
                    dto.setDeletedAt(deletedUserVideo.getDeletedAt());
                    dto.setPurgeAt(deletedUserVideo.getPurgeAt());

                    dto.setTitle(video.getTitle());
                    dto.setThumbnailUrl(video.getThumbnailUrl());
                    dto.setChannelTitle(video.getChannelTitle());

                    return dto;
                }).toList();
    }

    public List<TrashItemDisplayDTO> getDeletedUserPlaylists(String userId) {

        return deletedUserPlaylistRepository
                .findAllByUserPlaylistUserId(userId)
                .stream()
                .map(deletedUserPlaylist -> {

                    TrashItemDisplayDTO dto = new TrashItemDisplayDTO();

                    UserPlaylist userPlaylist = deletedUserPlaylist.getUserPlaylist();

                    Playlist playlist = deletedPlaylistRepository
                            .findByRestoreId(deletedUserPlaylist.getRestoreId())
                            .map(DeletedPlaylist::getPlaylist)
                            .orElseGet(() ->
                                    playlistRepository
                                            .findById(userPlaylist.getPlaylistId())
                                            .orElseThrow(RuntimeException::new)
                            );

                    dto.setId(deletedUserPlaylist.getId());
                    dto.setRestoreId(deletedUserPlaylist.getRestoreId());
                    dto.setAddedAt(userPlaylist.getAddedAt());
                    dto.setDeletedAt(deletedUserPlaylist.getDeletedAt());
                    dto.setPurgeAt(deletedUserPlaylist.getPurgeAt());

                    dto.setTitle(playlist.getTitle());
                    dto.setThumbnailUrl(playlist.getThumbnailUrl());
                    dto.setChannelTitle(playlist.getChannelTitle());

                    return dto;
                }).toList();
    }

    public List<TrashItemDisplayDTO> getDeletedNotes(String userId){
        return deletedNoteRepository
                .findAllByNoteUserId(userId)
                .stream()
                .map(deletedNote -> {
                    TrashItemDisplayDTO dto = new TrashItemDisplayDTO();

                    Note note = deletedNote.getNote();

                    dto.setId(deletedNote.getId());
                    dto.setRestoreId(deletedNote.getRestoreId());
                    dto.setAddedAt(note.getAddedAt());
                    dto.setDeletedAt(deletedNote.getDeletedAt());
                    dto.setPurgeAt(deletedNote.getPurgeAt());

                    dto.setTitle(note.getTitle());
                    dto.setThumbnailUrl(null);
                    dto.setChannelTitle(null);

                    return dto;
                }).toList();
    }

    public List<TrashItemDisplayDTO> getDeletedCodices(String userId){
        return deletedCodexRepository
                .findAllByCodexUserId(userId)
                .stream()
                .map(deletedCodex -> {
                    TrashItemDisplayDTO dto = new TrashItemDisplayDTO();

                    Codex codex = deletedCodex.getCodex();

                    dto.setId(deletedCodex.getId());
                    dto.setRestoreId(deletedCodex.getRestoreId());
                    dto.setAddedAt(codex.getAddedAt());
                    dto.setDeletedAt(deletedCodex.getDeletedAt());
                    dto.setPurgeAt(deletedCodex.getPurgeAt());

                    dto.setTitle(codex.getTitle());
                    dto.setThumbnailUrl(null);
                    dto.setChannelTitle(null);

                    return dto;
                }).toList();
    }

    //=========================
    // PURGE
    //=========================
    public void purgeVideos(List<String> restoreIds){
        for (String id : restoreIds){purgeVideo(id);}
    }

    public void purgePlaylists(List<String> restoreIds){
        for (String id : restoreIds){purgePlaylist(id);}
    }

    public void purgeNotes(List<String> restoreIds){
        for (String id : restoreIds){purgeNote(id);}
    }

    public void purgeCodices(List<String> restoreIds){
        for (String id : restoreIds){purgeCodex(id);}
    }

    private void purgeCodex(String restoreId){
        deletedCodexRepository.deleteByRestoreId(restoreId);
    }

    private void purgeNote(String restoreId){
        deletedNoteRepository.deleteByRestoreId(restoreId);
    }

    private void purgeVideo(String restoreId){
        deletedUserVideoRepository.deleteByRestoreId(restoreId);
        if (deletedVideoRepository.existsByRestoreId(restoreId))
            deletedVideoRepository.deleteByRestoreId(restoreId);
    }

    private void purgePlaylist(String restoreId){
        DeletedUserPlaylist deletedUserPlaylist = deletedUserPlaylistRepository.findByRestoreId(restoreId).orElseThrow(RuntimeException::new);
        List<DeletedUserPlaylistItem> deletedUserPlaylistItems = deletedUserPlaylistItemRepository.findAllByRestoreId(restoreId);
        for (DeletedUserPlaylistItem item : deletedUserPlaylistItems){
            purgeVideo(restoreId);
            deletedPlaylistItemRepository.deleteByRestoreId(restoreId);
            deletedUserPlaylistItemRepository.delete(item);
        }
        deletedUserPlaylistRepository.delete(deletedUserPlaylist);
    }

    //=========================
    // PURGE EXPIRED
    //=========================
    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    public void purgeExpired() {
        AppSettings appSettings = appSettingsService.getAppSettings();
        int deletionPeriod = appSettings.getDeletionPeriod();
        
        Instant now = Instant.now();
        Instant purgeThreshold = now.minus(deletionPeriod, ChronoUnit.DAYS);
        
        // Purge expired deleted videos
        deletedVideoRepository.deleteAllByPurgeAtBefore(purgeThreshold);
        
        // Purge expired deleted user videos
        deletedUserVideoRepository.deleteAllByPurgeAtBefore(purgeThreshold);
        
        // Purge expired deleted playlists
        deletedPlaylistRepository.deleteAllByPurgeAtBefore(purgeThreshold);
        
        // Purge expired deleted user playlists
        deletedUserPlaylistRepository.deleteAllByPurgeAtBefore(purgeThreshold);
        
        // Purge expired deleted playlist items
        deletedPlaylistItemRepository.deleteAllByPurgeAtBefore(purgeThreshold);
        
        // Purge expired deleted user playlist items
        deletedUserPlaylistItemRepository.deleteAllByPurgeAtBefore(purgeThreshold);
        
        // Purge expired deleted notes
        deletedNoteRepository.deleteAllByPurgeAtBefore(purgeThreshold);
        
        // Purge expired deleted codices
        deletedCodexRepository.deleteAllByPurgeAtBefore(purgeThreshold);
    }
}
