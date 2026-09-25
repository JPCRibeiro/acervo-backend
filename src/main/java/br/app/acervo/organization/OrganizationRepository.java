package br.app.acervo.organization;

import br.app.acervo.organization.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsByInviteCode(String inviteCode);

    Optional<Organization> findByInviteCode(String inviteCode);
}
