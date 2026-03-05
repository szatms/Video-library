package io.github.szatms.videolibrary.model.usermodel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private UserResponseDTO user;
    private String token;
}
