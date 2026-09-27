package br.app.acervo.membership.service;

import br.app.acervo.membership.domain.Membership;
import br.app.acervo.membership.domain.Role;
import br.app.acervo.membership.dto.MemberResponse;
import br.app.acervo.membership.dto.OrganizationSummary;
import br.app.acervo.membership.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;

    @Transactional
    public Membership add(UUID userId, UUID organizationId, Role role) {
        return membershipRepository.save(Membership.create(userId, organizationId, role));
    }

    @Transactional(readOnly = true)
    public Membership resolveDefault(UUID userId) {
        return membershipRepository.findFirstByUserIdOrderByLastAccessedAtDescJoinedAtAsc(userId)
                .orElseThrow(() -> new IllegalStateException("Usuário sem organização associada"));
    }

    @Transactional(readOnly = true)
    public Optional<Membership> find(UUID userId, UUID organizationId) {
        return membershipRepository.findByUserIdAndOrganizationId(userId, organizationId);
    }

    @Transactional
    public void touchAccess(Membership membership) {
        membership.touchLastAccess();
        membershipRepository.save(membership);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(UUID organizationId) {
        return membershipRepository.findMembersByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public List<OrganizationSummary> listOrganizations(UUID userId) {
        return membershipRepository.findOrganizationsByUserId(userId);
    }
}