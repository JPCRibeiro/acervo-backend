package br.app.acervo;

import br.app.acervo.user.domain.Role;
import br.app.acervo.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserTest {
    private User validUser() {
        return User.create(UUID.randomUUID(), "user@acme.com", "hash", "João", Role.OWNER);
    }

    @Test
    void shouldCreateValidUser() {
        User user = validUser();

        assertThat(user.getEmail()).isEqualTo("user@acme.com");
        assertThat(user.getName()).isEqualTo("João");
        assertThat(user.getRole()).isEqualTo(Role.OWNER);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectInvalidUser() {
        assertThatThrownBy(() -> User.create(null, "u@a.com", "hash", "João", Role.OWNER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> User.create(UUID.randomUUID(), " ", "hash", "João", Role.OWNER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> User.create(UUID.randomUUID(), "u@a.com", " ", "João", Role.OWNER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> User.create(UUID.randomUUID(), "u@a.com", "hash", " ", Role.OWNER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> User.create(UUID.randomUUID(), "u@a.com", "hash", "João", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}