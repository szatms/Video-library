package io.github.szatms.videolibrary.model.usermodel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {
    @NotBlank(message = "A username must be given.")
    private String username;

    @NotBlank(message = "A password must be set.")
    @Size(min = 6, message = "Your password must be at least 6 characters long.")
    private String password;
}
