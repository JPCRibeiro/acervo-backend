package br.app.acervo.user.service;

import br.app.acervo.user.domain.Role;
import br.app.acervo.user.domain.User;
import br.app.acervo.user.exception.EmailAlreadyExistsException;
import br.app.acervo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User create(UUID organizationId, String email, String rawPassword, String name, Role role) {
        String normalizedEmail = email.toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        return userRepository.save(
                User.create(
                        organizationId,
                        normalizedEmail,
                        passwordEncoder.encode(rawPassword),
                        name.trim(),
                        role
                )
        );
    }
}
