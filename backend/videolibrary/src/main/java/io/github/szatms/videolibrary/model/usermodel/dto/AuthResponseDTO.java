package io.github.szatms.videolibrary.model.usermodel.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private UserResponseDTO user;
    private String token;
}
