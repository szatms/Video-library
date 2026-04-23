package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.UserMapper;
import io.github.szatms.videolibrary.model.usermodel.User;
import io.github.szatms.videolibrary.model.usermodel.UserRepository;
import io.github.szatms.videolibrary.model.usermodel.dto.UserResponseDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserSelfUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    //=========================
    // CURRENT USER
    //=========================
    public UserResponseDTO getCurrentUser() {
        return userMapper.toResponseDTO(getCurrentUserEntity());
    }

    //=========================
    // SELF UPDATE
    //=========================
    public UserResponseDTO updateSelf(UserSelfUpdateDTO dto) {
        User user = getCurrentUserEntity();

        if (dto.getUsername() != null) {
            String newUsername = dto.getUsername().trim();
            if (newUsername.isEmpty())
                throw new IllegalArgumentException("Username cannot be empty");

            if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
                throw new IllegalArgumentException("Username already exists");
            }

            user.setUsername(newUsername);
        }

        if (dto.getPassword() != null) {
            if (dto.getPassword().isBlank())
                throw new IllegalArgumentException("Password cannot be empty");

            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }

    //=========================
    // SELF DELETE
    //=========================
    public void deleteSelf() {
        User user = getCurrentUserEntity();
        userRepository.deleteById(user.getUserId());
    }

    private User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null)
            throw new IllegalStateException("Not authenticated");

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof io.github.szatms.videolibrary.security.CustomUserDetails userDetails))
            throw new IllegalStateException("Not authenticated");

        String userId = userDetails.getUser().getUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
