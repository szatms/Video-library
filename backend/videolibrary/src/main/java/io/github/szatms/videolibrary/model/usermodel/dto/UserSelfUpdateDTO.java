package io.github.szatms.videolibrary.model.usermodel.dto;

import io.github.szatms.videolibrary.model.usermodel.DateFormatPreference;
import lombok.Data;

@Data
public class UserSelfUpdateDTO {
    private String username;
    private String password;
    private DateFormatPreference dateFormat;
}
