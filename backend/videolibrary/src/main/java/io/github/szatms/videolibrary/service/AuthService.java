package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.UserMapper;
import io.github.szatms.videolibrary.model.usermodel.Role;
import io.github.szatms.videolibrary.model.usermodel.User;
import io.github.szatms.videolibrary.model.usermodel.UserRepository;
import io.github.szatms.videolibrary.model.usermodel.dto.AuthResponseDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserCreateDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserLoginDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserResponseDTO;
import io.github.szatms.videolibrary.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    //=========================
    // REGISTRATION
    //=========================
    public AuthResponseDTO register(UserCreateDTO dto){
        if(userRepository.findByUsername(dto.getUsername()).isPresent())
            throw new IllegalArgumentException("Username already exists");

        String passwordHash = passwordEncoder.encode(dto.getPassword());

        User user = userMapper.fromCreateDTO(dto,  passwordHash);
        if (userRepository.count() == 0)
            user.setRole(Role.ADMIN);
        else
            user.setRole(Role.USER);

        userRepository.save(user);

        //return new AuthResponseDTO(user, user.getRole());
        //***************PLACEHOLDER***************
        UserResponseDTO responseUser = userMapper.toResponseDTO(user);
        return new AuthResponseDTO(responseUser, null);
        //***************PLACEHOLDER***************
    }

    //=========================
    // LOGIN
    //=========================
    public AuthResponseDTO login(UserLoginDTO dto){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        UserResponseDTO responseUser = userMapper.toResponseDTO(user);
        return new AuthResponseDTO(responseUser, null);
    }
}
