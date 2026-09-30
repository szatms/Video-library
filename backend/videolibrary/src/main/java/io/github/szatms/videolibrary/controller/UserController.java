package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.model.usermodel.dto.UserSelfUpdateDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserResponseDTO;
import io.github.szatms.videolibrary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateSelf(@RequestBody UserSelfUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateSelf(dto));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteSelf() {
        userService.deleteSelf();
        return ResponseEntity.noContent().build();
    }
}
