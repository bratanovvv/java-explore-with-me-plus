package ru.practicum.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Пользователь.
 * <p>
 * Состав ответа задаётся {@link JsonView}:
 * {@link ShortUser} — краткая информация для вложенных объектов, {@link FullUser} — полные данные.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    public interface ShortUser {
    }

    public interface FullUser extends ShortUser {
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonView(ShortUser.class)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 250)
    @JsonView(ShortUser.class)
    private String name;

    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    @JsonView(FullUser.class)
    private String email;
}
