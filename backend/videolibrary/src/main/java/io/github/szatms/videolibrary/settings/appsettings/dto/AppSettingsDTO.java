package io.github.szatms.videolibrary.settings.appsettings.dto;

import io.github.szatms.videolibrary.settings.DateFormat;
import io.github.szatms.videolibrary.settings.TimeFormat;
import lombok.Data;

import java.time.Instant;

@Data
public class AppSettingsDTO {
    private String id;
    private Integer deletionPeriod;
    private Integer updatePeriod;
    private DateFormat dateFormat;
    private TimeFormat timeFormat;
    private Integer maxUsers;
    private Instant createdAt;
    private Instant updatedAt;
    private String updatedById;
}
