package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.*;
import io.github.szatms.videolibrary.model.notemodel.Note;
import io.github.szatms.videolibrary.model.notemodel.NoteRepository;
import io.github.szatms.videolibrary.model.notemodel.dto.AddToNoteDTO;
import io.github.szatms.videolibrary.model.notemodel.dto.AddToParentDTO;
import io.github.szatms.videolibrary.model.notemodel.dto.NoteUpdateDTO;
import io.github.szatms.videolibrary.model.notemodel.dto.ParentResponseDTO;
import io.github.szatms.videolibrary.model.playlistmodel.PlaylistRepository;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoRepository;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylistRepository;
import io.github.szatms.videolibrary.model.videomodel.VideoRepository;
import io.github.szatms.videolibrary.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final UserVideoRepository userVideoRepository;
    private final UserPlaylistRepository userPlaylistRepository;
    private final UserVideoMapper userVideoMapper;
    private final VideoMapper videoMapper;
    private final VideoRepository videoRepository;
    private final UserPlaylistMapper userPlaylistMapper;
    private final PlaylistMapper playlistMapper;
    private final PlaylistRepository playlistRepository;
    private final StringUtils stringUtils;
    private final TrashService trashService;

    public Note createNote(String userId, List<String> parentIds, String title, String content, boolean isPdf, String pdfUrl){
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");

        // Sanitize the title to prevent filename issues
        String sanitizedTitle = stringUtils.checkTitle(title);

        Note note = Note.builder()
                .userId(userId)
                .parentIds(parentIds)
                .title(sanitizedTitle)
                .addedAt(Instant.now())
                .build();

        if (isPdf){
            note.setIsPdf(true);
            note.setContent(null);
            note.setPdfUrl(pdfUrl);
        } else {
            note.setIsPdf(false);
            note.setContent(content);
            note.setPdfUrl(null);
        }

        return noteRepository.save(note);
    }

    public void addToNote(String userId, AddToNoteDTO dto){
        String noteId = dto.getNoteId();
        List<String> parentIds = dto.getParentIds();
        
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");
        if (noteId == null || noteId.isBlank())
            throw new IllegalArgumentException("Invalid noteId");

        Note note = getNote(userId, noteId);

        for (String parentId:parentIds){
            if (!note.getParentIds().contains(parentId) && !parentId.isBlank()) {
                note.getParentIds().add(parentId);
                noteRepository.save(note);
                
                // Update parent entities (UserVideo or UserPlaylist) to include note ID
                // Check if parentId corresponds to a UserVideo
                userVideoRepository.findById(parentId)
                        .filter(userVideo -> userVideo.getUserId().equals(userId))
                        .ifPresent(userVideo -> {
                            if (!userVideo.getNoteIds().contains(noteId)) {
                                userVideo.getNoteIds().add(noteId);
                                userVideoRepository.save(userVideo);
                            }
                        });
                
                // Check if parentId corresponds to a UserPlaylist
                userPlaylistRepository.findById(parentId)
                        .filter(userPlaylist -> userPlaylist.getUserId().equals(userId))
                        .ifPresent(userPlaylist -> {
                            if (!userPlaylist.getNoteIds().contains(noteId)) {
                                userPlaylist.getNoteIds().add(noteId);
                                userPlaylistRepository.save(userPlaylist);
                            }
                        });
            }
        }
    }

    public void addToParent(String userId, AddToParentDTO dto){
        String parentId = dto.getParentId();
        List<String> noteIds = dto.getNoteIds();
        
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");
        if (parentId == null || parentId.isBlank())
            throw new IllegalArgumentException("Invalid parentId");

        // Check if parentId corresponds to a UserVideo
        userVideoRepository.findById(parentId)
                .filter(userVideo -> userVideo.getUserId().equals(userId))
                .ifPresent(userVideo -> {
                    for (String noteId : noteIds) {
                        if (!noteId.isBlank()) {
                            // Get the note to ensure it exists and belongs to the user
                            Note note = getNote(userId, noteId);
                            
                            // Add parent to note if not already present
                            if (!note.getParentIds().contains(parentId)) {
                                note.getParentIds().add(parentId);
                                noteRepository.save(note);
                                
                                // Update parent entity to include note ID
                                if (!userVideo.getNoteIds().contains(noteId)) {
                                    userVideo.getNoteIds().add(noteId);
                                    userVideoRepository.save(userVideo);
                                }
                            }
                        }
                    }
                });

        // Check if parentId corresponds to a UserPlaylist
        userPlaylistRepository.findById(parentId)
                .filter(userPlaylist -> userPlaylist.getUserId().equals(userId))
                .ifPresent(userPlaylist -> {
                    for (String noteId : noteIds) {
                        if (!noteId.isBlank()) {
                            // Get the note to ensure it exists and belongs to the user
                            Note note = getNote(userId, noteId);
                            
                            // Add parent to note if not already present
                            if (!note.getParentIds().contains(parentId)) {
                                note.getParentIds().add(parentId);
                                noteRepository.save(note);
                                
                                // Update parent entity to include note ID
                                if (!userPlaylist.getNoteIds().contains(noteId)) {
                                    userPlaylist.getNoteIds().add(noteId);
                                    userPlaylistRepository.save(userPlaylist);
                                }
                            }
                        }
                    }
                });
    }

    public void removeFromNote(String userId, List<String>  parentIds, String noteId){
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");
        if (noteId == null || noteId.isBlank())
            throw new IllegalArgumentException("Invalid noteId");

        Note note = getNote(userId, noteId);

        for (String parentId : parentIds) {
            if (note.getParentIds().contains(parentId)){
                note.getParentIds().remove(parentId);
                noteRepository.save(note);
                
                // Update parent entities (UserVideo or UserPlaylist) to remove note ID
                // Check if parentId corresponds to a UserVideo
                userVideoRepository.findById(parentId)
                        .filter(userVideo -> userVideo.getUserId().equals(userId))
                        .ifPresent(userVideo -> {
                            if (userVideo.getNoteIds().contains(noteId)) {
                                userVideo.getNoteIds().remove(noteId);
                                userVideoRepository.save(userVideo);
                            }
                        });
                
                // Check if parentId corresponds to a UserPlaylist
                userPlaylistRepository.findById(parentId)
                        .filter(userPlaylist -> userPlaylist.getUserId().equals(userId))
                        .ifPresent(userPlaylist -> {
                            if (userPlaylist.getNoteIds().contains(noteId)) {
                                userPlaylist.getNoteIds().remove(noteId);
                                userPlaylistRepository.save(userPlaylist);
                            }
                        });
            } else {
                throw new IllegalArgumentException("Invalid parentId, note isn't connected to parent.");
            }
        }
    }

    public void removeFromParent(String userId, AddToParentDTO dto){
        String parentId = dto.getParentId();
        List<String> noteIds = dto.getNoteIds();
        
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");
        if (parentId == null || parentId.isBlank())
            throw new IllegalArgumentException("Invalid parentId");

        // Check if parentId corresponds to a UserVideo
        userVideoRepository.findById(parentId)
                .filter(userVideo -> userVideo.getUserId().equals(userId))
                .ifPresent(userVideo -> {
                    for (String noteId : noteIds) {
                        if (!noteId.isBlank()) {
                            // Get the note to ensure it exists and belongs to the user
                            Note note = getNote(userId, noteId);
                            
                            // Remove parent from note if it exists
                            if (note.getParentIds().contains(parentId)) {
                                note.getParentIds().remove(parentId);
                                noteRepository.save(note);
                                
                                // Update parent entity to remove note ID
                                if (userVideo.getNoteIds().contains(noteId)) {
                                    userVideo.getNoteIds().remove(noteId);
                                    userVideoRepository.save(userVideo);
                                }
                            }
                        }
                    }
                });

        // Check if parentId corresponds to a UserPlaylist
        userPlaylistRepository.findById(parentId)
                .filter(userPlaylist -> userPlaylist.getUserId().equals(userId))
                .ifPresent(userPlaylist -> {
                    for (String noteId : noteIds) {
                        if (!noteId.isBlank()) {
                            // Get the note to ensure it exists and belongs to the user
                            Note note = getNote(userId, noteId);
                            
                            // Remove parent from note if it exists
                            if (note.getParentIds().contains(parentId)) {
                                note.getParentIds().remove(parentId);
                                noteRepository.save(note);
                                
                                // Update parent entity to remove note ID
                                if (userPlaylist.getNoteIds().contains(noteId)) {
                                    userPlaylist.getNoteIds().remove(noteId);
                                    userPlaylistRepository.save(userPlaylist);
                                }
                            }
                        }
                    }
                });
    }

    public List<String> getParentIds(String userId, String noteId){
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");
        if (noteId == null || noteId.isBlank())
            throw new IllegalArgumentException("Invalid noteId");

        Note note = getNote(userId, noteId);
        return note.getParentIds();
    }

    public List<ParentResponseDTO> getParents(String userId, List<String> parentIds){
        List<ParentResponseDTO> parents = new ArrayList<>();
        for(String parentId : parentIds){
            userVideoRepository.findById(parentId)
                    .filter(userVideo -> userVideo.getUserId().equals(userId))
                    .ifPresent(userVideo -> {
                        parents.add(noteMapper.toParentResponseDTO(
                                userVideoMapper.toSummaryResponseDTO(
                                        userVideo,
                                        videoMapper.toSummaryDTO(videoRepository.getByVideoId(userVideo.getVideoId()))
                                        )));
                    });
            userPlaylistRepository.findById(parentId)
                    .filter(userPlaylist -> userPlaylist.getUserId().equals(userId))
                    .ifPresent(userPlaylist -> {
                        parents.add(noteMapper.toParentResponseDTO(
                                userPlaylistMapper.toSummaryDTO(
                                        userPlaylist,
                                        playlistMapper.toSummaryDTO(playlistRepository.getById(userPlaylist.getPlaylistId()))
                                )));
                    });
        }

        return parents;
    }

    public Note getNote(String userId, String noteId){
        return noteRepository.findById(noteId)
                .filter(note -> note.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalStateException("Note not found"));
    }

    public Note updateNote(String userid, String noteId, NoteUpdateDTO dto){
        Note note = getNote(userid, noteId);
        noteMapper.updateNoteFromDTO(note, dto);
        return noteRepository.save(note);
    }

    public List<Note> getNotes(String userId, List<String> parentIds){
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");
        
        if (parentIds == null || parentIds.isEmpty()) {
            // Return all notes for the user
            return noteRepository.findByUserId(userId);
        } else {
            // Return notes that belong to ALL specified parents (set intersection)
            List<Note> allNotes = noteRepository.findByUserId(userId);
            List<Note> result = new ArrayList<>();
            
            for (Note note : allNotes) {
                // Check if note belongs to ALL parents in parentIds list
                boolean belongsToAll = true;
                for (String parentId : parentIds) {
                    if (!note.getParentIds().contains(parentId)) {
                        belongsToAll = false;
                        break;
                    }
                }
                if (belongsToAll) {
                    result.add(note);
                }
            }
            return result;
        }
    }
    
    public List<Note> getNotesForParent(String userId, String parentId) {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("Invalid userId");
        if (parentId == null || parentId.isBlank())
            throw new IllegalArgumentException("Invalid parentId");
            
        return noteRepository.findByParentIdsInList(List.of(parentId));
    }

    public String generateNoteAsText(Note note) {
        StringBuilder content = new StringBuilder();
        content.append(note.getTitle()).append("\n\n");
        content.append(note.getContent());
        return content.toString();
    }
    
    public void deleteNote(String noteId){
        Note note = noteRepository.findById(noteId).orElseThrow(RuntimeException::new);
        String restoreId = UUID.randomUUID().toString();
        trashService.moveNoteToTrash(note, restoreId);
    }
}
