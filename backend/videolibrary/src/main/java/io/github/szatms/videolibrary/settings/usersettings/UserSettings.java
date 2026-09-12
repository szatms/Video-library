package io.github.szatms.videolibrary.settings.usersettings;

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
@Document(collection = "user_settings")
public class UserSettings {
    @Id
    private String userId;
    private DateFormat dateFormat;
    private TimeFormat timeFormat;
    private Instant createdAd;
    private Instant updatedAt;
    private String updatedById;
}
