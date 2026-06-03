package com.example.backend.mediator;

import com.example.backend.dto.user.ChangePasswordRequest;
import com.example.backend.dto.user.UpdateUserRequest;
import com.example.backend.dto.user.UserDto;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.foundation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        UserRole role = new UserRole();
        role.setId(1L);
        role.setName("USER");

        user = new User("john", encoder.encode("pass123"), "john@mail.com", role);
        user.setId(1L);
    }

    // ── getMe ──────────────────────────────────────────────────────────────────

    @Test
    void getMe_existingUser_returnsDto() {
        when(userRepository.findByLogin("john")).thenReturn(Optional.of(user));

        UserDto dto = userService.getMe("john");

        assertThat(dto.getLogin()).isEqualTo("john");
        assertThat(dto.getEmail()).isEqualTo("john@mail.com");
        assertThat(dto.isAdmin()).isFalse();
    }

    @Test
    void getMe_unknownLogin_throwsRuntimeException() {
        when(userRepository.findByLogin("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe("nobody"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ── updateMe ───────────────────────────────────────────────────────────────

    @Test
    void updateMe_newLogin_updatesLoginField() {
        when(userRepository.findByLogin("john")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setLogin("john_updated");

        userService.updateMe("john", req);

        assertThat(user.getLogin()).isEqualTo("john_updated");
        verify(userRepository).save(user);
    }

    @Test
    void updateMe_newEmail_updatesEmailField() {
        when(userRepository.findByLogin("john")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("new@mail.com");

        userService.updateMe("john", req);

        assertThat(user.getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    void updateMe_nullLogin_doesNotChangeLogin() {
        when(userRepository.findByLogin("john")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setLogin(null);

        userService.updateMe("john", req);

        assertThat(user.getLogin()).isEqualTo("john");
    }

    @Test
    void updateMe_blankLogin_doesNotChangeLogin() {
        when(userRepository.findByLogin("john")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setLogin("   ");

        userService.updateMe("john", req);

        assertThat(user.getLogin()).isEqualTo("john");
    }

    // ── changePassword ─────────────────────────────────────────────────────────

    @Test
    void changePassword_correctOldPassword_encodesNew() {
        when(userRepository.findByLogin("john")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("pass123");
        req.setNewPassword("newSecret99");

        userService.changePassword("john", req);

        assertThat(encoder.matches("newSecret99", user.getPassword())).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_wrongOldPassword_throwsAndDoesNotSave() {
        when(userRepository.findByLogin("john")).thenReturn(Optional.of(user));

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("wrongPassword");
        req.setNewPassword("newSecret99");

        assertThatThrownBy(() -> userService.changePassword("john", req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Wrong current password");

        verify(userRepository, never()).save(any());
    }
}
