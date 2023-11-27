package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class InclusionTypeTest {

    @Test
    public void inclusionTypeIn() {
        assertThat(InclusionType.IN, equalTo(InclusionType.IN));
        assertThat(InclusionType.IN.value(), equalTo("in"));
    }

    @Test
    public void inclusionTypeOut() {
        assertThat(InclusionType.OUT, equalTo(InclusionType.OUT));
        assertThat(InclusionType.OUT.value(), equalTo("out"));
    }
}
