package br.app.acervo.auth.service;

import br.app.acervo.auth.dto.AccessTokenResponse;
import br.app.acervo.auth.dto.JoinRequest;
import br.app.acervo.auth.dto.LoginRequest;
import br.app.acervo.auth.dto.RegisterRequest;
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

    @Transactional
    public AccessTokenResponse register(RegisterRequest req) {
        Organization organization = organizationService.create(req.organizationName());
        User user = userService.create(organization.getId(), req.email(), req.password(), req.name(), Role.OWNER);

        return issueToken(user);
    }

    @Transactional
    public AccessTokenResponse join(JoinRequest req) {
        Organization organization = organizationService.getByInviteCode(req.inviteCode());
        User user = userService.create(
                organization.getId(), req.email(), req.password(), req.name(), Role.MEMBER
        );
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public AccessTokenResponse login(LoginRequest req) {
        User user = userService.authenticate(req.email(), req.password());
        return issueToken(user);
    }

    private AccessTokenResponse issueToken(User user) {
        String token = tokenService.issue(user);
        return AccessTokenResponse.bearer(token, tokenService.getExpiresInSeconds());
    }
}

