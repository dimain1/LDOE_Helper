package com.example.backend.mediator;

import com.example.backend.dto.user.ChangePasswordRequest;
import com.example.backend.dto.user.UpdateUserRequest;
import com.example.backend.dto.user.UserDto;
import com.example.backend.entity.User;
import com.example.backend.foundation.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getMe(String login) {
        User user = findByLogin(login);
        return toDto(user);
    }

    @Transactional
    public UserDto updateMe(String login, UpdateUserRequest request) {
        User user = findByLogin(login);

        if (request.getLogin() != null && !request.getLogin().isBlank()) {
            user.setLogin(request.getLogin());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String login, ChangePasswordRequest request) {
        User user = findByLogin(login);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong current password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found: " + login));
    }

    public static UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getLogin(), user.getEmail(), user.isAdmin());
    }
}
