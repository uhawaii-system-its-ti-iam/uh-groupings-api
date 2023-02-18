package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class PreferenceStatusTest {

    @Test
    public void PreferenceStatusEnable() {
        assertThat(PreferenceStatus.ENABLE, equalTo(PreferenceStatus.ENABLE));
        assertThat(PreferenceStatus.ENABLE.value(), equalTo("enable"));
        assertThat(PreferenceStatus.ENABLE.toggle(), equalTo(true));
    }

    @Test
    public void PreferenceStatusDisable() {
        assertThat(PreferenceStatus.DISABLE, equalTo(PreferenceStatus.DISABLE));
        assertThat(PreferenceStatus.DISABLE.value(), equalTo("disable"));
        assertThat(PreferenceStatus.DISABLE.toggle(), equalTo(false));
    }

    @Test
    public void find() {
        PreferenceStatus enablePreferenceStatus = PreferenceStatus.find("enable");
        assertThat(enablePreferenceStatus, equalTo(PreferenceStatus.ENABLE));

        PreferenceStatus disablePreferenceStatus = PreferenceStatus.find("disable");
        assertThat(disablePreferenceStatus, equalTo(PreferenceStatus.DISABLE));

        String badValue = "bogus";
        PreferenceStatus preferenceStatus = PreferenceStatus.find(badValue);
        assertThat(preferenceStatus, nullValue());
    }
}
