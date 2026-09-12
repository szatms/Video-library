package io.github.szatms.videolibrary.model.uservideomodel.dto;

import io.github.szatms.videolibrary.model.uservideomodel.Timestamp;
import lombok.Data;

import java.util.List;

@Data
public class UserVideoUpdateDTO {
    private boolean watched;
    private List<Timestamp> timestamps;
}
