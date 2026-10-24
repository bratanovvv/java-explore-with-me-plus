package ru.practicum.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void create_shouldSaveAndReturnUser() {
        User newUser = new User(null, "Иван Петров", "ivan@example.com");
        User savedUser = new User(1L, "Иван Петров", "ivan@example.com");
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(false);
        when(userRepository.save(newUser)).thenReturn(savedUser);

        User actual = userService.create(newUser);

        assertThat(actual).isEqualTo(savedUser);
        verify(userRepository).save(newUser);
    }

    @Test
    void create_withExistingEmail_shouldThrowConflict() {
        User newUser = new User(null, "Иван Петров", "ivan@example.com");
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(newUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ivan@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUsers_withoutIds_shouldReturnPage() {
        User user = new User(1L, "Иван Петров", "ivan@example.com");
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));

        List<User> actual = userService.getUsers(null, new PageParams(0, 10));

        assertThat(actual).containsExactly(user);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void getUsers_withNonMultipleFrom_shouldReturnRequestedWindow() {
        List<User> loaded = IntStream.rangeClosed(10, 24)
                .mapToObj(i -> new User((long) i, "User " + i, "user" + i + "@example.com"))
                .toList();
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(loaded));

        List<User> actual = userService.getUsers(null, new PageParams(15, 10));

        assertThat(actual).hasSize(10);
        assertThat(actual.get(0).getId()).isEqualTo(15L);
        assertThat(actual.get(9).getId()).isEqualTo(24L);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(15);
    }

    @Test
    void getUsers_withEmptyIds_shouldReturnPage() {
        User user = new User(1L, "Иван Петров", "ivan@example.com");
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));

        List<User> actual = userService.getUsers(List.of(), new PageParams(0, 10));

        assertThat(actual).containsExactly(user);
        verify(userRepository, never()).findAllByIdIn(any(), any());
    }

    @Test
    void getUsers_withIds_shouldFilterByIds() {
        User user = new User(1L, "Иван Петров", "ivan@example.com");
        when(userRepository.findAllByIdIn(eq(List.of(1L)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        List<User> actual = userService.getUsers(List.of(1L), new PageParams(0, 10));

        assertThat(actual).containsExactly(user);
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void delete_withExistingUser_shouldDelete() {
        User user = new User(1L, "Иван Петров", "ivan@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void delete_withMissingUser_shouldThrowNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
        verify(userRepository, never()).delete(any());
    }
}
