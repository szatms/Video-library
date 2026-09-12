package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.NoteMapper;
import io.github.szatms.videolibrary.model.notemodel.Note;
import io.github.szatms.videolibrary.model.notemodel.dto.*;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteService noteService;
    private final NoteMapper noteMapper;

    @GetMapping
    public List<NoteResponseDTO> getNotes(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) List<String> parentIds
            ) {
        String userId = userDetails.getUser().getUserId();
        List<Note> notes = noteService.getNotes(userId, parentIds);
        return notes.stream()
                .map(noteMapper::toResponseDTO)
                .toList();
    }

    @PostMapping("/create")
    public NoteResponseDTO createNote(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody NoteCreateDTO dto
    ) {
        String userId = userDetails.getUser().getUserId();
        Note note = noteService.createNote(userId, dto.getParentIds(), dto.getTitle(), dto.getContent(), dto.getIsPdf() != null && dto.getIsPdf(), dto.getPdfUrl());
        return noteMapper.toResponseDTO(note);
    }

    @PostMapping("/add")
    public void addNote(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody AddToNoteDTO dto
    ) {
        String userId = userDetails.getUser().getUserId();
        noteService.addToNote(userId, dto);
    }

    @DeleteMapping("/delete/{noteId}")
    public void deleteNote(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable String noteId
    ) {
        String userId = userDetails.getUser().getUserId();
        noteService.deleteNote(noteId);
    }

    @PutMapping("/remove")
    public void removeNote(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody AddToNoteDTO dto
    ) {
        String userId = userDetails.getUser().getUserId();
        noteService.removeFromNote(userId, dto.getParentIds(), dto.getNoteId());
    }

    @PutMapping("/{noteId}")
    public NoteSummaryResponseDTO updateNote(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable String noteId,
      @RequestBody NoteUpdateDTO dto
    ) {
        String userId = userDetails.getUser().getUserId();
        Note note = noteService.updateNote(userId, noteId, dto);
        return noteMapper.toSummaryResponseDTO(note);
    }

    @GetMapping("/{noteId}/parents")
    public List<ParentResponseDTO> getNoteParents(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable String noteId
    ) {
        String userId = userDetails.getUser().getUserId();
        return noteService.getParents(userId, noteService.getParentIds(userId, noteId));
    }

    @GetMapping(value = "/{noteId}/download", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Resource> downloadNote(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String noteId
    ) {
        String userId = userDetails.getUser().getUserId();
        Note note = noteService.getNote(userId, noteId);
        String content = noteService.generateNoteAsText(note);

        ByteArrayResource resource = new ByteArrayResource(content.getBytes());

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + note.getTitle() + ".txt\"")
                .body(resource);
    }
}
