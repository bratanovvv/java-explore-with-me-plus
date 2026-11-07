package ru.practicum.user.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;
import ru.practicum.utils.params.PageParams;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public AdminUserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    @JsonView(UserDto.FullUser.class)
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = PageParams.DEFAULT_FROM) @PositiveOrZero int from,
            @RequestParam(defaultValue = PageParams.DEFAULT_SIZE) @Positive int size) {
        List<User> users = userService.getUsers(ids, new PageParams(from, size));
        return userMapper.toDtoList(users);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(UserDto.FullUser.class)
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        User user = userService.create(userMapper.toEntity(userDto));
        return userMapper.toDto(user);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
