package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    public void authorityNameAppliesTheRolePrefix() {
        assertThat(Role.ADMIN.authorityName(), equalTo("ROLE_ADMIN"));
        assertThat(Role.OWNER.authorityName(), equalTo("ROLE_OWNER"));
        assertThat(Role.UH.authorityName(), equalTo("ROLE_UH"));
        assertThat(Role.ANONYMOUS.authorityName(), equalTo("ROLE_ANONYMOUS"));
        assertThat(Role.DEPARTMENTAL.authorityName(), equalTo("ROLE_DEPARTMENTAL"));
    }

    @Test
    public void fromClaimResolvesThePlainContractForm() {
        assertThat(Role.fromClaim("ADMIN"), equalTo(Optional.of(Role.ADMIN)));
        assertThat(Role.fromClaim("OWNER"), equalTo(Optional.of(Role.OWNER)));
        assertThat(Role.fromClaim("UH"), equalTo(Optional.of(Role.UH)));
    }

    @Test
    public void fromClaimResolvesThePrefixedFormSentByOlderUiBuilds() {
        assertThat(Role.fromClaim("ROLE_ADMIN"), equalTo(Optional.of(Role.ADMIN)));
        assertThat(Role.fromClaim("ROLE_OWNER"), equalTo(Optional.of(Role.OWNER)));
    }

    @Test
    public void fromClaimIgnoresSurroundingWhitespaceAndCase() {
        assertThat(Role.fromClaim("  admin  "), equalTo(Optional.of(Role.ADMIN)));
        assertThat(Role.fromClaim("role_admin"), equalTo(Optional.of(Role.ADMIN)));
    }

    @Test
    public void fromClaimRejectsUnknownNames() {
        assertTrue(Role.fromClaim("ADMINISTRATOR").isEmpty());
        assertTrue(Role.fromClaim("ROLE_ADMINISTRATOR").isEmpty());
        assertTrue(Role.fromClaim("SUPERUSER").isEmpty());
        assertTrue(Role.fromClaim("").isEmpty());
        assertTrue(Role.fromClaim(null).isEmpty());
    }

    @Test
    public void fromClaimDoesNotStripMoreThanOnePrefix() {
        assertTrue(Role.fromClaim("ROLE_ROLE_ADMIN").isEmpty());
    }

    @Test
    public void everyRoleRoundTripsThroughItsAuthorityName() {
        for (Role role : Role.values()) {
            assertThat(Role.fromClaim(role.name()), equalTo(Optional.of(role)));
            assertThat(Role.fromClaim(role.authorityName()), equalTo(Optional.of(role)));
        }
    }

    @Test
    public void roleNamesMatchTheUiContract() {
        // These names are the wire contract with ui/src/lib/access/role.ts; renaming one
        // here silently drops the matching authority for every user that carries it.
        assertThat(Role.values().length, equalTo(5));
        assertFalse(Role.fromClaim("DEPARTMENT").isPresent());
        assertTrue(Role.fromClaim("DEPARTMENTAL").isPresent());
    }
}
