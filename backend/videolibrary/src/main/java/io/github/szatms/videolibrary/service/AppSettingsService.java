package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.AppSettingsMapper;
import io.github.szatms.videolibrary.settings.DateFormat;
import io.github.szatms.videolibrary.settings.TimeFormat;
import io.github.szatms.videolibrary.settings.appsettings.AppSettings;
import io.github.szatms.videolibrary.settings.appsettings.AppSettingsRepository;
import io.github.szatms.videolibrary.settings.appsettings.dto.AppSettingsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppSettingsService {
    private final AppSettingsRepository appSettingsRepository;
    private final AppSettingsMapper appSettingsMapper;

    public AppSettings getAppSettings(){
        List<AppSettings> allSettings = appSettingsRepository.findAll();
        return allSettings.isEmpty() ? null : allSettings.get(0);
    }

    public AppSettings updateAppSettings(AppSettingsDTO dto){
        AppSettings appSettings = getAppSettings();
        appSettingsMapper.updateEntityFromDTO(appSettings, dto);
        return appSettingsRepository.save(appSettings);
    }

    public AppSettings initializeAppSettings(){
        AppSettings appSettings = new AppSettings();

        appSettings.setDeletionPeriod(30);
        appSettings.setUpdatePeriod(7);
        appSettings.setDateFormat(DateFormat.EU);
        appSettings.setTimeFormat(TimeFormat.H_24);
        appSettings.setMaxUsers(15);
        appSettings.setCreatedAt(Instant.now());
        appSettings.setUpdatedAt(appSettings.getCreatedAt());
        appSettings.setUpdatedById(null);

        return appSettingsRepository.save(appSettings);
    }
}
