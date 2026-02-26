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

    //=========================
    // CREATE DTO --> ENTITY
    //=========================
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

    //=========================
    // ENTITY --> RESPONSE DTO
    //=========================
    public UserResponseDTO toResponseDTO(User user){
        UserResponseDTO dto = new UserResponseDTO();

        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    //=========================
    // ADMIN UPDATE DTO --> ENTITY
    //=========================
    public void updateEntityFromAdminDTO(UserAdminUpdateDTO dto, User user){
        if(dto.getRole() != null)
            user.setRole(dto.getRole());

        if(dto.getEnabled() != null)
            user.setEnabled(dto.getEnabled());
    }

    //=========================
    // USER UPDATE DTO --> ENTITY
    //=========================
    public void updateEntityFromUserDTO(UserSelfUpdateDTO dto, User user){
        if(dto.getUsername() != null)
            user.setUsername(dto.getUsername());

        if (dto.getPassword() != null)
            user.setPasswordHash(dto.getPassword());
    }
}
