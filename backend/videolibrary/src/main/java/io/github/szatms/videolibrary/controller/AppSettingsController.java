package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.security.CustomUserDetails;
import io.github.szatms.videolibrary.service.AppSettingsService;
import io.github.szatms.videolibrary.settings.appsettings.AppSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settings/app")
public class AppSettingsController {
    private final AppSettingsService appSettingsService;

    @GetMapping
    public AppSettings getAppSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUser().getUserId();
        return appSettingsService.getAppSettings();
    }
}
