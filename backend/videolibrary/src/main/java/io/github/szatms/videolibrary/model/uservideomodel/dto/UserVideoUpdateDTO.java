package io.github.szatms.videolibrary.model.uservideomodel.dto;

import lombok.Data;

@Data
public class UserVideoUpdateDTO {
    private String note;
    private boolean watched;
}
