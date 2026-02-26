package io.github.szatms.videolibrary.model.usermodel.dto;

import io.github.szatms.videolibrary.model.usermodel.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Integer userId;
    private String username;
    private Role role;
    private LocalDateTime createdAt;
    private Boolean enabled;
}
