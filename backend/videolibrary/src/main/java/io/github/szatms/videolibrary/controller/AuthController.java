package io.github.szatms.videolibrary.controller;

import io.github.szatms.videolibrary.model.usermodel.dto.AuthResponseDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserCreateDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserLoginDTO;
import io.github.szatms.videolibrary.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    //=========================
    // REGISTRATION
    //=========================
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody UserCreateDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    //=========================
    // LOGIN
    //=========================
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UserLoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
