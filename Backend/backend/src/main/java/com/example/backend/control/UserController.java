package com.example.backend.control;

import com.example.backend.dto.user.ChangePasswordRequest;
import com.example.backend.dto.user.UpdateUserRequest;
import com.example.backend.dto.user.UserDto;
import com.example.backend.mediator.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Получить данные текущего пользователя. */
    @GetMapping("/me")
    public UserDto getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getMe(userDetails.getUsername());
    }

    /** Обновить логин / email. */
    @PutMapping("/me")
    public UserDto updateMe(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestBody UpdateUserRequest request) {
        return userService.updateMe(userDetails.getUsername(), request);
    }

    /** Сменить пароль. */
    @PutMapping("/me/password")
    public void changePassword(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
    }
}
