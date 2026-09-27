package br.app.acervo.user.service;

import br.app.acervo.user.domain.User;
import br.app.acervo.user.dto.UserProfileResponse;
import br.app.acervo.user.exception.EmailAlreadyExistsException;
import br.app.acervo.user.exception.InvalidCredentialsException;
import br.app.acervo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User create(String email, String rawPassword, String name) {
        String normalizedEmail = email.toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        return userRepository.save(
                User.create(normalizedEmail, passwordEncoder.encode(rawPassword), name.trim())
        );
    }

    @Transactional(readOnly = true)
    public User authenticate(String email, String rawPassword) {
        String normalizedEmail = email.toLowerCase().trim();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        return userRepository.findById(userId)
                .map(u -> new UserProfileResponse(u.getId(), u.getName(), u.getEmail()))
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }
}
