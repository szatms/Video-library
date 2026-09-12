package io.github.szatms.videolibrary.settings.usersettings.dto;

import io.github.szatms.videolibrary.settings.DateFormat;
import io.github.szatms.videolibrary.settings.TimeFormat;
import lombok.Data;

import java.time.Instant;

@Data
public class UserSettingsDTO {
    private String userId;
    private DateFormat dateFormat;
    private TimeFormat timeFormat;
    private Instant createdAd;
    private Instant updatedAt;
    private String updatedById;
}
