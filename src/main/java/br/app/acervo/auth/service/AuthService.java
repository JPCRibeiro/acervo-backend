package br.app.acervo.auth.service;

import br.app.acervo.auth.dto.JoinRequest;
import br.app.acervo.auth.dto.LoginRequest;
import br.app.acervo.auth.dto.RegisterRequest;
import br.app.acervo.auth.exception.InvalidRefreshTokenException;
import br.app.acervo.organization.domain.Organization;
import br.app.acervo.organization.service.OrganizationService;
import br.app.acervo.user.domain.Role;
import br.app.acervo.user.domain.User;
import br.app.acervo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final OrganizationService organizationService;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public record AuthResult(String accessToken, String refreshToken, long expiresIn) {}

    @Transactional
    public AuthResult register(RegisterRequest req) {
        Organization organization = organizationService.create(req.organizationName());
        User user = userService.create(organization.getId(), req.email(), req.password(), req.name(), Role.OWNER);

        return issueTokens(user);
    }

    @Transactional
    public AuthResult join(JoinRequest req) {
        Organization organization = organizationService.getByInviteCode(req.inviteCode());
        User user = userService.create(
                organization.getId(), req.email(), req.password(), req.name(), Role.MEMBER
        );
        return issueTokens(user);
    }

    @Transactional
    public AuthResult login(LoginRequest req) {
        User user = userService.authenticate(req.email(), req.password());
        return issueTokens(user);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(rawRefreshToken);
        User user = userService.findById(rotation.userId())
                .orElseThrow(InvalidRefreshTokenException::new);
        String accessToken = tokenService.issue(user);
        return new AuthResult(accessToken, rotation.rawToken(), tokenService.getExpiresInSeconds());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeAllByRawToken(rawRefreshToken);
    }

    private AuthResult issueTokens(User user) {
        String accessToken = tokenService.issue(user);
        String refreshToken = refreshTokenService.issue(user.getId());
        return new AuthResult(accessToken, refreshToken, tokenService.getExpiresInSeconds());
    }
}

