package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.settings.usersettings.UserSettings;
import io.github.szatms.videolibrary.settings.usersettings.dto.UserSettingsDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserSettingsMapper {
    //=========================
    // DTO --> ENTITY
    //=========================
    public void updateEntityFromDTO(UserSettings userSettings, UserSettingsDTO dto){
        userSettings.setDateFormat(dto.getDateFormat());
        userSettings.setTimeFormat(dto.getTimeFormat());
        userSettings.setUpdatedAt(Instant.now());
        userSettings.setUpdatedById(dto.getUpdatedById());
    }
}
