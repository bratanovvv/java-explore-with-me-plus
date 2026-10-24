package ru.practicum.user.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;
import ru.practicum.utils.interfaces.Mapper;

@Component
public class UserMapper implements Mapper<User, UserDto> {

    @Override
    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail());
    }

    @Override
    public User toEntity(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail());
    }
}
