package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.CodexMapper;
import io.github.szatms.videolibrary.model.codexmodel.Codex;
import io.github.szatms.videolibrary.model.codexmodel.CodexSortBy;
import io.github.szatms.videolibrary.model.codexmodel.SortDirection;
import io.github.szatms.videolibrary.model.codexmodel.dto.*;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.service.CodexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/codices")
public class CodexController {
    private final CodexService codexService;
    private final CodexMapper codexMapper;

    @GetMapping
    public List<CodexSummaryResponseDTO> getCodices(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) CodexSortBy sortBy,
            @RequestParam(required = false) SortDirection direction
    ) {
        String userId = userDetails.getUser().getUserId();
        List<Codex> codices = codexService.getCodices(userId, sortBy, direction);
        return codices.stream()
                .map(codexMapper::toCodexSummaryResponseDTO)
                .toList();
    }

    @PostMapping("/create")
    public CodexResponseDTO createCodex(
            @RequestBody CodexCrudDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        Codex codex = codexService.createCodex(userId, dto.getTitle(), dto.getDescription(), dto.getContentIds());
        return codexMapper.toCodexResponseDTO(codex);
    }

    @PostMapping("/add")
    public void addToCodex(
            @RequestBody CodexCrudDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        codexService.addToCodex(userId, dto);
    }

    @PutMapping("/update")
    public void updateCodex(
            @RequestBody CodexCrudDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        codexService.updateCodex(userId, dto);
    }

    @PutMapping("/remove")
    public void removeFromCodex(
            @RequestBody CodexCrudDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        codexService.removeFromCodex(userId, dto);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCodex(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        // Verify that the codex belongs to the authenticated user
        codexService.getCodex(userId, id);
        codexService.deleteCodex(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public CodexResponseDTO getCodex(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        Codex codex = codexService.getCodex(userId, id);
        return codexMapper.toCodexResponseDTO(codex);
    }

    @GetMapping("/{id}/content")
    public List<ContentResponseDTO> getCodexContent(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        // Verify that the codex belongs to the authenticated user
        codexService.getCodex(userId, id);
        return codexService.getContent(userId, codexService.getCodex(userId, id).getContentIds());
    }
}
