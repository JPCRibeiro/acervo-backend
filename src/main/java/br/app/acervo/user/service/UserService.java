package br.app.acervo.user.service;

import br.app.acervo.user.domain.Role;
import br.app.acervo.user.domain.User;
import br.app.acervo.user.dto.MemberResponse;
import br.app.acervo.user.exception.EmailAlreadyExistsException;
import br.app.acervo.user.exception.InvalidCredentialsException;
import br.app.acervo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
                User.create(organizationId, normalizedEmail, passwordEncoder.encode(rawPassword), name.trim(), role)
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
    public MemberResponse getProfile(UUID userId) {
        return userRepository.findById(userId)
                .map(u -> new MemberResponse(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listByOrganization(UUID organizationId) {
        return userRepository.findByOrganizationIdOrderByCreatedAtAsc(organizationId)
                .stream()
                .map(u -> new MemberResponse(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .toList();
    }
}
