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
    public void authorityNameFromClaimPrefixesThePlainContractForm() {
        assertThat(Role.authorityNameFromClaim("ADMIN"), equalTo(Optional.of("ROLE_ADMIN")));
        assertThat(Role.authorityNameFromClaim("OWNER"), equalTo(Optional.of("ROLE_OWNER")));
        assertThat(Role.authorityNameFromClaim("UH"), equalTo(Optional.of("ROLE_UH")));
    }

    @Test
    public void authorityNameFromClaimLeavesThePrefixedFormAlone() {
        assertThat(Role.authorityNameFromClaim("ROLE_ADMIN"), equalTo(Optional.of("ROLE_ADMIN")));
        assertThat(Role.authorityNameFromClaim("ROLE_OWNER"), equalTo(Optional.of("ROLE_OWNER")));
    }

    @Test
    public void authorityNameFromClaimIgnoresSurroundingWhitespaceAndCase() {
        assertThat(Role.authorityNameFromClaim("  admin  "), equalTo(Optional.of("ROLE_ADMIN")));
        assertThat(Role.authorityNameFromClaim("role_admin"), equalTo(Optional.of("ROLE_ADMIN")));
    }

    @Test
    public void authorityNameFromClaimRejectsUnknownNames() {
        assertTrue(Role.authorityNameFromClaim("ADMINISTRATOR").isEmpty());
        assertTrue(Role.authorityNameFromClaim("ROLE_ADMINISTRATOR").isEmpty());
        assertTrue(Role.authorityNameFromClaim("SUPERUSER").isEmpty());
        assertTrue(Role.authorityNameFromClaim("").isEmpty());
        assertTrue(Role.authorityNameFromClaim(null).isEmpty());
    }

    @Test
    public void authorityNameFromClaimNeverDoublePrefixes() {
        // A prefixed claim skips the prefixing step, so it never picks up a second one.
        assertTrue(Role.authorityNameFromClaim("ROLE_ROLE_ADMIN").isEmpty());
        assertThat(Role.authorityNameFromClaim("ROLE_ADMIN"), equalTo(Optional.of("ROLE_ADMIN")));
    }

    @Test
    public void everyRoleIsResolvedFromBothFormsOfItsClaim() {
        for (Role role : Role.values()) {
            assertThat(Role.authorityNameFromClaim(role.name()),
                    equalTo(Optional.of(role.authorityName())));
            assertThat(Role.authorityNameFromClaim(role.authorityName()),
                    equalTo(Optional.of(role.authorityName())));
        }
    }

    @Test
    public void roleNamesMatchTheUiContract() {
        // Wire contract with ui/src/lib/access/role.ts; renaming one here silently drops
        // the matching authority for every user that carries it.
        assertThat(Role.values().length, equalTo(5));
        assertFalse(Role.authorityNameFromClaim("DEPARTMENT").isPresent());
        assertTrue(Role.authorityNameFromClaim("DEPARTMENTAL").isPresent());
    }
}
