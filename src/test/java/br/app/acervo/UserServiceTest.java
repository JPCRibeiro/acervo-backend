package br.app.acervo;

import br.app.acervo.user.domain.Role;
import br.app.acervo.user.domain.User;
import br.app.acervo.user.exception.EmailAlreadyExistsException;
import br.app.acervo.user.exception.InvalidCredentialsException;
import br.app.acervo.user.repository.UserRepository;
import br.app.acervo.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void shouldCreateUserNormalizingEmailAndEncodingPassword() {
        when(userRepository.existsByEmail("user@acme.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.create(
                UUID.randomUUID(), "  USER@Acme.com ", "secret123", "João", Role.OWNER);

        assertThat(result.getEmail()).isEqualTo("user@acme.com");
        assertThat(result.getPasswordHash()).isEqualTo("hashed");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("user@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(
                UUID.randomUUID(), "user@acme.com", "secret123", "João", Role.OWNER))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldAuthenticateWithValidCredentials() {
        User user = User.create(UUID.randomUUID(), "user@acme.com", "hashed", "João", Role.OWNER);
        when(userRepository.findByEmail("user@acme.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);

        assertThat(userService.authenticate("user@acme.com", "secret123")).isSameAs(user);
    }

    @Test
    void shouldRejectAuthenticationWhenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate("missing@acme.com", "secret123"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldRejectAuthenticationWhenPasswordDoesNotMatch() {
        User user = User.create(UUID.randomUUID(), "user@acme.com", "hashed", "João", Role.OWNER);
        when(userRepository.findByEmail("user@acme.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticate("user@acme.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}