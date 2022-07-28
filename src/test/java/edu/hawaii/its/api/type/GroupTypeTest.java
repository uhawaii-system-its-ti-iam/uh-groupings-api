package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class GroupTypeTest {

    @Test
    public void groupTypeInclude() {
        assertThat(GroupType.INCLUDE, equalTo(GroupType.INCLUDE));
        assertThat(GroupType.INCLUDE.value(), equalTo(":include"));
    }

    @Test
    public void groupTypeExclude() {
        assertThat(GroupType.EXCLUDE, equalTo(GroupType.EXCLUDE));
        assertThat(GroupType.EXCLUDE.value(), equalTo(":exclude"));
    }

    @Test
    public void find() {
        GroupType includeGroupType = GroupType.find(":include");
        assertThat(includeGroupType, equalTo(GroupType.INCLUDE));

        GroupType excludeGroupType = GroupType.find(":exclude");
        assertThat(excludeGroupType, equalTo(GroupType.EXCLUDE));

        // Undefined find.
        String badValue = "what?";
        GroupType groupType = GroupType.find(badValue);
        assertThat(groupType, nullValue());
    }
}

