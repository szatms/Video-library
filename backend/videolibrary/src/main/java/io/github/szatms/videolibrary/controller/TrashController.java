package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.model.trashmodel.DeletedUserVideo;
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

    @PostMapping("/restore/{restoreId}")
    public ResponseEntity<Void> restore(
            @PathVariable String restoreId
    ) {
        trashService.restore(restoreId);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<TrashItemDisplayDTO> getDeletedUserVideos(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return trashService.getDeletedUserVideos(
                userDetails.getUser().getUserId()
        );
    }
}