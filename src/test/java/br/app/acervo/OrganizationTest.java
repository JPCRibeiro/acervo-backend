package br.app.acervo;

import br.app.acervo.organization.domain.Organization;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrganizationTest {
    @Test
    void shouldCreateValidOrganization() {
        Organization org = Organization.create("Acme", "ABCD1234");

        assertThat(org.getName()).isEqualTo("Acme");
        assertThat(org.getInviteCode()).isEqualTo("ABCD1234");
        assertThat(org.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectInvalidOrganization() {
        assertThatThrownBy(() -> Organization.create(" ", "ABCD1234"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Organization.create("Acme", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}