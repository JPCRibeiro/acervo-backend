package br.app.acervo;

import br.app.acervo.user.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserTest {
    private User validUser() {
        return User.create("user@acme.com", "hash", "João");
    }

    @Test
    void shouldCreateValidUser() {
        User user = validUser();

        assertThat(user.getEmail()).isEqualTo("user@acme.com");
        assertThat(user.getName()).isEqualTo("João");
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectInvalidUser() {
        assertThatThrownBy(() -> User.create(null, "hash", "João"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> User.create(" ", "hash", "João"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> User.create("u@a.com", null, "João"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> User.create("u@a.com", " ", "João"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> User.create("u@a.com", "hash", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> User.create("u@a.com", "hash", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}