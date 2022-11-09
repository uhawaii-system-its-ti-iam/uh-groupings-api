package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class PreferenceTypeTest {

    @Test
    public void preferenceTypeEnable() {
        assertThat(PreferenceType.ENABLE, equalTo(PreferenceType.ENABLE));
        assertThat(PreferenceType.ENABLE.value(), equalTo("enable"));
        assertThat(PreferenceType.ENABLE.toggleOn(), equalTo(true));
    }

    @Test
    public void preferenceTypeDisable() {
        assertThat(PreferenceType.DISABLE, equalTo(PreferenceType.DISABLE));
        assertThat(PreferenceType.DISABLE.value(), equalTo("disable"));
        assertThat(PreferenceType.DISABLE.toggleOn(), equalTo(false));
    }

    @Test
    public void find() {
        PreferenceType enablePreferenceType = PreferenceType.find("enable");
        assertThat(enablePreferenceType, equalTo(PreferenceType.ENABLE));

        PreferenceType disablePreferenceType = PreferenceType.find("disable");
        assertThat(disablePreferenceType, equalTo(PreferenceType.DISABLE));

        String badValue = "bogus";
        PreferenceType preferenceType = PreferenceType.find(badValue);
        assertThat(preferenceType, nullValue());
    }
}
