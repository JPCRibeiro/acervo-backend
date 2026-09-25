package br.app.acervo;

import br.app.acervo.organization.OrganizationRepository;
import br.app.acervo.organization.domain.Organization;
import br.app.acervo.organization.exception.InvalidInviteCodeException;
import br.app.acervo.organization.service.InviteCodeGenerator;
import br.app.acervo.organization.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {
    @Mock
    OrganizationRepository organizationRepository;

    @Mock
    InviteCodeGenerator inviteCodeGenerator;

    @InjectMocks
    OrganizationService organizationService;

    @Test
    void shouldCreateOrganizationTrimmingNameWithUniqueCode() {
        when(inviteCodeGenerator.generate()).thenReturn("CODE1234");
        when(organizationRepository.existsByInviteCode("CODE1234")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        Organization result = organizationService.create("  Acme  ");

        assertThat(result.getName()).isEqualTo("Acme");
        assertThat(result.getInviteCode()).isEqualTo("CODE1234");
        verify(inviteCodeGenerator).generate();
    }

    @Test
    void shouldRetryUntilInviteCodeIsUnique() {
        when(inviteCodeGenerator.generate()).thenReturn("DUP12345", "UNIQ6789");
        when(organizationRepository.existsByInviteCode("DUP12345")).thenReturn(true);
        when(organizationRepository.existsByInviteCode("UNIQ6789")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        Organization result = organizationService.create("Acme");

        assertThat(result.getInviteCode()).isEqualTo("UNIQ6789");
        verify(inviteCodeGenerator, times(2)).generate();
    }

    @Test
    void shouldReturnOrganizationByInviteCode() {
        Organization org = Organization.create("Acme", "CODE1234");
        when(organizationRepository.findByInviteCode("CODE1234")).thenReturn(Optional.of(org));

        assertThat(organizationService.getByInviteCode("CODE1234")).isSameAs(org);
    }

    @Test
    void shouldThrowWhenInviteCodeNotFound() {
        when(organizationRepository.findByInviteCode(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.getByInviteCode("MISSING1"))
                .isInstanceOf(InvalidInviteCodeException.class);
    }
}