package org.example.service;

import org.example.entity.User;
import org.example.exception.InvalidRequestException;
import org.example.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String register(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        if (userRepository.findByUsername(username).isPresent()) {
            return "This username is already taken. Please choose another one.";
        }
        userRepository.save(user);
        return "User registered successfully";
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found!"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidRequestException("Wrong password!");
        }
        return jwtService.generateToken(username);
    }
}
