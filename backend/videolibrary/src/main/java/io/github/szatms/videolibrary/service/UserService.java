package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.UserMapper;
import io.github.szatms.videolibrary.model.usermodel.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
}
