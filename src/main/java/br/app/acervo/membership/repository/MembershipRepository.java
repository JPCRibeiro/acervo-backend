package br.app.acervo.membership.repository;

import br.app.acervo.membership.domain.Membership;
import br.app.acervo.membership.dto.MemberResponse;
import br.app.acervo.membership.dto.OrganizationSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    Optional<Membership> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    Optional<Membership> findFirstByUserIdOrderByLastAccessedAtDescJoinedAtAsc(UUID userId);

    @Query("""
        SELECT new br.app.acervo.membership.dto.MemberResponse(u.id, u.name, u.email, m.role)
        FROM Membership m JOIN User u ON m.userId = u.id
        WHERE m.organizationId = :organizationId
        ORDER BY m.joinedAt
        """)
    List<MemberResponse> findMembersByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("""
    SELECT new br.app.acervo.membership.dto.OrganizationSummary(o.id, o.name, m.role)
    FROM Membership m JOIN Organization o ON m.organizationId = o.id
    WHERE m.userId = :userId
    ORDER BY m.joinedAt
    """)
    List<OrganizationSummary> findOrganizationsByUserId(@Param("userId") UUID userId);
}