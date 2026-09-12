package io.github.szatms.videolibrary.settings.appsettings;

import io.github.szatms.videolibrary.settings.DateFormat;
import io.github.szatms.videolibrary.settings.TimeFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "app_settings")
public class AppSettings {
    @Id
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
