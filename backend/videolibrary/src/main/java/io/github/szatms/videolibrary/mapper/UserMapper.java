package io.github.szatms.videolibrary.mapper;

import io.github.szatms.videolibrary.model.usermodel.Role;
import io.github.szatms.videolibrary.model.usermodel.User;
import io.github.szatms.videolibrary.model.usermodel.dto.UserCreateDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserResponseDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserAdminUpdateDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserSelfUpdateDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {
    public User fromCreateDTO(UserCreateDTO dto, String passwordHash){
        return User.builder()
                .userId(null)
                .username(dto.getUsername())
                .passwordHash(passwordHash)
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public UserResponseDTO toResponseDTO(User user){
        UserResponseDTO dto = new UserResponseDTO();

        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());

        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
