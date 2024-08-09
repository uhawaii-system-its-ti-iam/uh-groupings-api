package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OotbMemberTest {

    private OotbMember member;

    @BeforeEach
    public void setUp() {
        member = new OotbMember();
    }

    @Test
    public void construction() {
        assertNotNull(member);
    }

    @Test
    public void name() {
        assertNull(member.getName());
        member.setName("John Doe");
        assertThat(member.getName(), is("John Doe"));
    }

    @Test
    public void uhUuid() {
        assertNull(member.getUhUuid());
        member.setUhUuid("12345678");
        assertThat(member.getUhUuid(), is("12345678"));
    }

    @Test
    public void uid() {
        assertNull(member.getUid());
        member.setUid("jdoe");
        assertThat(member.getUid(), is("jdoe"));
    }

    @Test
    public void allArgsConstructor() {
        OotbMember member = new OotbMember("Jane Doe", "87654321", "jadoe");
        assertThat(member.getName(), is("Jane Doe"));
        assertThat(member.getUhUuid(), is("87654321"));
        assertThat(member.getUid(), is("jadoe"));
    }

    @Test
    public void noArgsConstructor() {
        OotbMember member = new OotbMember();
        assertNull(member.getName());
        assertNull(member.getUhUuid());
        assertNull(member.getUid());
    }
}
