package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.mapper.UserSettingsMapper;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.service.UserSettingsService;
import io.github.szatms.videolibrary.settings.usersettings.UserSettings;
import io.github.szatms.videolibrary.settings.usersettings.dto.UserSettingsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settings/user")
public class UserSettingsController {
    private final UserSettingsService userSettingsService;

    @GetMapping
    public UserSettings getUserSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        return userSettingsService.getUserSettings(userId);
    }

    @PutMapping
    public UserSettings updateUserSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserSettingsDTO dto
            ) {
        String userId = userDetails.getUser().getUserId();
        return userSettingsService.updateUserSettings(userId, dto);
    }

    @PostMapping("/init")
    public UserSettings initializeUserSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        String userId = userDetails.getUser().getUserId();
        return userSettingsService.initializeUserSettings(userId);
    }
}
