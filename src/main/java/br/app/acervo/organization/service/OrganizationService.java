package br.app.acervo.organization.service;

import br.app.acervo.organization.OrganizationRepository;
import br.app.acervo.organization.domain.Organization;
import br.app.acervo.organization.exception.InvalidInviteCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final InviteCodeGenerator inviteCodeGenerator;

    @Transactional
    public Organization create(String name) {
        return organizationRepository.save(
                Organization.create(name.trim(), generateUniqueInviteCode())
        );
    }

    @Transactional(readOnly = true)
    public Organization getByInviteCode(String inviteCode) {
        return organizationRepository.findByInviteCode(inviteCode)
                .orElseThrow(InvalidInviteCodeException::new);
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            code = inviteCodeGenerator.generate();
        } while (organizationRepository.existsByInviteCode(code));
        return code;
    }
}
