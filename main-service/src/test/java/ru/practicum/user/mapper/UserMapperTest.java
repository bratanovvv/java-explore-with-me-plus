package ru.practicum.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toDto_shouldMapAllFields() {
        User user = new User(1L, "Иван Петров", "ivan@example.com");

        UserDto userDto = userMapper.toDto(user);

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("Иван Петров");
        assertThat(userDto.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void toEntity_shouldMapAllFields() {
        UserDto userDto = new UserDto(1L, "Иван Петров", "ivan@example.com");

        User user = userMapper.toEntity(userDto);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Иван Петров");
        assertThat(user.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void toDtoList_shouldMapAllUsers() {
        List<User> users = List.of(
                new User(1L, "Иван Петров", "ivan@example.com"),
                new User(2L, "Пётр Иванов", "petr@example.com"));

        List<UserDto> userDtos = userMapper.toDtoList(users);

        assertThat(userDtos).hasSize(2);
        assertThat(userDtos).extracting(UserDto::getId).containsExactly(1L, 2L);
    }
}
