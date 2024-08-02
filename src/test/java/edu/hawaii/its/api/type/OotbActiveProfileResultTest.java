package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OotbActiveProfileResultTest {

    private OotbActiveProfile ootbActiveProfile;

    @BeforeEach
    public void setUp() {
        ootbActiveProfile = new OotbActiveProfile();
        ootbActiveProfile.setUid("admin0123");
        ootbActiveProfile.setUhUuid("33333333");
    }

    @Test
    public void constructionWithProfile() {
        OotbActiveProfileResult result = new OotbActiveProfileResult(ootbActiveProfile);
        assertNotNull(result);
        assertThat(result.getResultCode(), is("SUCCESS"));
        assertNotNull(result.getResult());
        assertThat(result.getResult().getUid(), is("admin0123"));
        assertThat(result.getResult().getUhUuid(), is("33333333"));
    }

    @Test
    public void constructionWithoutProfile() {
        OotbActiveProfileResult result = new OotbActiveProfileResult();
        assertNotNull(result);
        assertThat(result.getResultCode(), is("FAILURE"));
        assertNull(result.getResult());
    }

    @Test
    public void resultCode() {
        OotbActiveProfileResult result = new OotbActiveProfileResult();
        result.setResultCode("CUSTOM_CODE");
        assertThat(result.getResultCode(), is("CUSTOM_CODE"));
    }

    @Test
    public void result() {
        OotbActiveProfileResult result = new OotbActiveProfileResult();
        assertNull(result.getResult());
        result.setResult(ootbActiveProfile);
        assertNotNull(result.getResult());
        assertThat(result.getResult().getUid(), is("admin0123"));
        assertThat(result.getResult().getUhUuid(), is("33333333"));
    }
}
