package io.github.szatms.videolibrary.model.statsmodel;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelStats {
    private Long viewCount;
    private Long subCount;
    private Long videoCount;
    private Instant updatedAt;
}
