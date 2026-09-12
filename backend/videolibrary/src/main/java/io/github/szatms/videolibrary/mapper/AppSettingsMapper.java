package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.settings.appsettings.AppSettings;
import io.github.szatms.videolibrary.settings.appsettings.dto.AppSettingsDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AppSettingsMapper {
    //=========================
    // DTO --> ENTITY
    //=========================
    public void updateEntityFromDTO(AppSettings appSettings, AppSettingsDTO dto){
        appSettings.setDeletionPeriod(dto.getDeletionPeriod());
        appSettings.setUpdatePeriod(dto.getUpdatePeriod());
        appSettings.setDateFormat(dto.getDateFormat());
        appSettings.setTimeFormat(dto.getTimeFormat());
        appSettings.setMaxUsers(dto.getMaxUsers());
        appSettings.setUpdatedAt(Instant.now());
        appSettings.setUpdatedById(dto.getUpdatedById());
    }
}
