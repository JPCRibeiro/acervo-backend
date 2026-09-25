package br.app.acervo.organization.service;

import br.app.acervo.organization.dto.InviteCodeResponse;
import br.app.acervo.organization.dto.OrganizationResponse;
import br.app.acervo.organization.exception.OrganizationNotFoundException;
import br.app.acervo.organization.repository.OrganizationRepository;
import br.app.acervo.organization.domain.Organization;
import br.app.acervo.organization.exception.InvalidInviteCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    @Transactional(readOnly = true)
    public OrganizationResponse getById(UUID organizationId) {
        Organization organization = findOrThrow(organizationId);
        return new OrganizationResponse(organization.getId(), organization.getName());
    }

    @Transactional(readOnly = true)
    public InviteCodeResponse getInviteCode(UUID organizationId) {
        return new InviteCodeResponse(findOrThrow(organizationId).getInviteCode());
    }

    private Organization findOrThrow(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(OrganizationNotFoundException::new);
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            code = inviteCodeGenerator.generate();
        } while (organizationRepository.existsByInviteCode(code));
        return code;
    }
}
