package io.github.szatms.videolibrary.model.uservideomodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Timestamp {
    private Integer seconds;
    private String label;
}
