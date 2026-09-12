package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.model.trashmodel.dto.TrashItemDisplayDTO;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.service.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trash")
public class TrashController {

    private final TrashService trashService;

    @PostMapping("/restore/videos/{restoreId}")
    public ResponseEntity<Void> restoreVideos(
            @PathVariable String restoreId
    ) {
        trashService.restoreVideos(restoreId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/videos")
    public List<TrashItemDisplayDTO> getDeletedUserVideos(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return trashService.getDeletedUserVideos(
                userDetails.getUser().getUserId()
        );
    }

    @DeleteMapping("/delete/videos")
    public ResponseEntity<Void> purgeVideos(
            @RequestBody List<String> restoreIds
    ) {
        trashService.purgeVideos(restoreIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/playlists")
    public ResponseEntity<Void> purgePlaylists(
            @RequestBody List<String> restoreIds
    ) {
        trashService.purgePlaylists(restoreIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/notes")
    public ResponseEntity<Void> purgeNotes(
            @RequestBody List<String> restoreIds
    ) {
        trashService.purgeNotes(restoreIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/codices")
    public ResponseEntity<Void> purgeCodices(
            @RequestBody List<String> restoreIds
    ) {
        trashService.purgeCodices(restoreIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore/playlists/{restoreId}")
    public ResponseEntity<Void> restorePlaylists(
            @PathVariable String restoreId
    ) {
        trashService.restorePlaylists(restoreId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/playlists")
    public List<TrashItemDisplayDTO> getDeletedUserPlaylists(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return trashService.getDeletedUserPlaylists(
                userDetails.getUser().getUserId()
        );
    }

    @PostMapping("/restore/notes/{restoreId}")
    public ResponseEntity<Void> restoreNotes(
            @PathVariable String restoreId
    ) {
        trashService.restoreNote(restoreId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/notes")
    public List<TrashItemDisplayDTO> getDeletedNotes(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return trashService.getDeletedNotes(
                userDetails.getUser().getUserId()
        );
    }

    @PostMapping("/restore/codices/{restoreId}")
    public ResponseEntity<Void> restoreCodices(
            @PathVariable String restoreId
    ) {
        trashService.restoreCodex(restoreId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/codices")
    public List<TrashItemDisplayDTO> getDeletedCodices(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return trashService.getDeletedCodices(
                userDetails.getUser().getUserId()
        );
    }
}