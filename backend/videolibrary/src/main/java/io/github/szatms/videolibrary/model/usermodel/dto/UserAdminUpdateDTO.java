package io.github.szatms.videolibrary.model.usermodel.dto;

import io.github.szatms.videolibrary.model.usermodel.Role;
import lombok.Data;

@Data
public class UserAdminUpdateDTO {
    private Boolean enabled;
    private Role role;
}
