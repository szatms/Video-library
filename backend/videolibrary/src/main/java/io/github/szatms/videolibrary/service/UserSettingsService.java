package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.UserSettingsMapper;
import io.github.szatms.videolibrary.settings.appsettings.AppSettings;
import io.github.szatms.videolibrary.settings.usersettings.UserSettings;
import io.github.szatms.videolibrary.settings.usersettings.UserSettingsRepository;
import io.github.szatms.videolibrary.settings.usersettings.dto.UserSettingsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserSettingsService {
    private final UserSettingsRepository userSettingsRepository;
    private final UserSettingsMapper userSettingsMapper;
    private final AppSettingsService appSettingsService;

    public UserSettings getUserSettings(String userId){
        return userSettingsRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("Settings not found"));
    }

    public UserSettings updateUserSettings(String userId, UserSettingsDTO dto){
        UserSettings userSettings = getUserSettings(userId);
        userSettingsMapper.updateEntityFromDTO(userSettings, dto);
        return userSettingsRepository.save(userSettings);
    }

    public UserSettings initializeUserSettings(String userId){
        AppSettings appSettings = appSettingsService.getAppSettings();
        UserSettings userSettings = new UserSettings();

        userSettings.setUserId(userId);
        userSettings.setDateFormat(appSettings.getDateFormat());
        userSettings.setTimeFormat(appSettings.getTimeFormat());
        userSettings.setCreatedAd(Instant.now());
        userSettings.setUpdatedAt(userSettings.getCreatedAd());
        userSettings.setUpdatedById(null);

        return userSettingsRepository.save(userSettings);
    }
}
