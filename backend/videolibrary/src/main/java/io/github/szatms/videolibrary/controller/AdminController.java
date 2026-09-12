package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.model.usermodel.dto.UserAdminUpdateDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserResponseDTO;
import io.github.szatms.videolibrary.model.videomodel.dto.VideoRefreshJobResponseDTO;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.service.AppSettingsService;
import io.github.szatms.videolibrary.service.UserService;
import io.github.szatms.videolibrary.service.RefreshService;
import io.github.szatms.videolibrary.settings.appsettings.AppSettings;
import io.github.szatms.videolibrary.settings.appsettings.dto.AppSettingsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final RefreshService refreshService;
    private final AppSettingsService appSettingsService;
    private final UserService userService;

    @PostMapping("/settings/app/init")
    @PreAuthorize("hasRole('OWNER')")
    public AppSettings initializeAppSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        return appSettingsService.initializeAppSettings();
    }

    @PostMapping("/settings/app/update")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public AppSettings updateAppSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AppSettingsDTO dto
    ) {
        String userId = userDetails.getUser().getUserId();
        return appSettingsService.updateAppSettings(dto);
    }
    
    @PutMapping("/users/update")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> adminUpdateUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserAdminUpdateDTO dto) {
        UserResponseDTO updatedUser = userService.adminUpdate(dto);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/users/delete")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<String> userIds) {
        userService.deleteByAdmin(userIds);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }
    
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<String> refreshAllVideosAndPlaylists() {
        refreshService.refreshAllVideosAndPlaylists();
        return ResponseEntity.ok("Refresh initiated successfully");
    }
}
