package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class GroupTypeTest {

    @Test
    public void groupTypeBasis() {
        assertThat(GroupType.BASIS, equalTo(GroupType.BASIS));
        assertThat(GroupType.BASIS.value(), equalTo(":basis"));
    }

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
    public void groupTypeOwners() {
        assertThat(GroupType.OWNERS, equalTo(GroupType.OWNERS));
        assertThat(GroupType.OWNERS.value(), equalTo(":owners"));
    }
}

