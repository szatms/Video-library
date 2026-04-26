package io.github.szatms.videolibrary.model.statsmodel;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stats {
    private long viewCount;
    private long likeCount;
    private Instant updatedAt;
}
