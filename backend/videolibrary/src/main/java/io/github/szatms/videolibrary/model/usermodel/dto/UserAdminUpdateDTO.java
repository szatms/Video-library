package io.github.szatms.videolibrary.model.usermodel.dto;

import io.github.szatms.videolibrary.model.usermodel.Role;
import lombok.Data;

@Data
public class UserAdminUpdateDTO {
    private String userId;
    private String username;
    private String password;
    private Boolean enabled;
    private Role role;
}
