package ru.practicum.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    public List<User> getUsers(List<Long> ids, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable(Sort.by(Sort.Direction.ASC, "id"));
        Page<User> users = CollectionUtils.isEmpty(ids)
                ? userRepository.findAll(pageable)
                : userRepository.findAllByIdIn(ids, pageable);
        return users.getContent();
    }

    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("User with email " + user.getEmail() + " already exists");
        }
        User savedUser = userRepository.save(user);
        log.debug("Создан пользователь id={}", savedUser.getId());
        return savedUser;
    }

    @Transactional
    public void delete(Long userId) {
        User user = getUser(userId);
        userRepository.delete(user);
        log.debug("Удалён пользователь id={}", userId);
    }
}
