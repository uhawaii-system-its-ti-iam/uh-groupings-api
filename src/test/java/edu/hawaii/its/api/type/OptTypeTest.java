package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class OptTypeTest {

    @Test
    public void optTypeIn() {
        assertThat(OptType.IN, equalTo(OptType.IN));
        String expectedInValue = "uh-settings:attributes:"
                + "for-groups:uh-grouping:anyone-can:opt-in";
        assertThat(OptType.IN.value(), equalTo(expectedInValue));
        assertThat(OptType.IN.inclusionType(), equalTo(InclusionType.IN));
    }

    @Test
    public void optTypeOut() {
        assertThat(OptType.OUT, equalTo(OptType.OUT));
        String expectedOutValue = "uh-settings:attributes:"
                + "for-groups:uh-grouping:anyone-can:opt-out";
        assertThat(OptType.OUT.value(), equalTo(expectedOutValue));
        assertThat(OptType.OUT.inclusionType(), equalTo(InclusionType.OUT));
    }

    @Test
    public void find() {
        String inOptValue = "uh-settings:attributes:"
                + "for-groups:uh-grouping:anyone-can:opt-in";
        OptType inOptType = OptType.find(inOptValue);
        assertThat(inOptType, equalTo(OptType.IN));

        String outOptValue = "uh-settings:attributes:"
                + "for-groups:uh-grouping:anyone-can:opt-out";
        OptType outOptType = OptType.find(outOptValue);
        assertThat(outOptType, equalTo(OptType.OUT));

        // Undefined find.
        String badValue = "what?";
        OptType optType = OptType.find(badValue);
        assertThat(optType, nullValue());
    }
}

