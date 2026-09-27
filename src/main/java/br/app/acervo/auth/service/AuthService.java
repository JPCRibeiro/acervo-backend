package br.app.acervo.auth.service;

import br.app.acervo.auth.dto.JoinRequest;
import br.app.acervo.auth.dto.LoginRequest;
import br.app.acervo.auth.dto.RegisterRequest;
import br.app.acervo.auth.exception.InvalidRefreshTokenException;
import br.app.acervo.membership.domain.Membership;
import br.app.acervo.membership.service.MembershipService;
import br.app.acervo.organization.domain.Organization;
import br.app.acervo.organization.service.OrganizationService;
import br.app.acervo.membership.domain.Role;
import br.app.acervo.user.domain.User;
import br.app.acervo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final OrganizationService organizationService;
    private final MembershipService membershipService;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public record AuthResult(String accessToken, String refreshToken, long expiresIn) {}

    @Transactional
    public AuthResult register(RegisterRequest req) {
        Organization org = organizationService.create(req.organizationName());
        User user = userService.create(req.email(), req.password(), req.name());
        Membership membership = membershipService.add(user.getId(), org.getId(), Role.OWNER);
        membershipService.touchAccess(membership);

        return issueTokens(user, membership);
    }

    @Transactional
    public AuthResult join(JoinRequest req) {
        Organization org = organizationService.getByInviteCode(req.inviteCode());
        User user = userService.create(req.email(), req.password(), req.name());
        Membership membership = membershipService.add(user.getId(), org.getId(), Role.MEMBER);
        membershipService.touchAccess(membership);

        return issueTokens(user, membership);
    }

    @Transactional
    public AuthResult login(LoginRequest req) {
        User user = userService.authenticate(req.email(), req.password());
        Membership membership = membershipService.resolveDefault(user.getId());
        membershipService.touchAccess(membership);

        return issueTokens(user, membership);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken, UUID requestedOrganizationId) {
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(rawRefreshToken);
        User user = userService.findById(rotation.userId())
                .orElseThrow(InvalidRefreshTokenException::new);

        Membership membership = (requestedOrganizationId != null)
                ? membershipService.find(user.getId(), requestedOrganizationId)
                .orElseGet(() -> membershipService.resolveDefault(user.getId()))
                : membershipService.resolveDefault(user.getId());

        String accessToken = tokenService.issue(user, membership);
        return new AuthResult(accessToken, rotation.rawToken(), tokenService.getExpiresInSeconds());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeAllByRawToken(rawRefreshToken);
    }

    private AuthResult issueTokens(User user, Membership membership) {
        String accessToken = tokenService.issue(user, membership);
        String refreshToken = refreshTokenService.issue(user.getId());
        return new AuthResult(accessToken, refreshToken, tokenService.getExpiresInSeconds());
    }
}

