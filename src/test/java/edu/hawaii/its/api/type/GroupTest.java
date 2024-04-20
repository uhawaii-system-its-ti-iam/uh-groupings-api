package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.wrapper.Subject;

import org.springframework.test.util.ReflectionTestUtils;

public class GroupTest {

    private Group group;

    @BeforeEach
    public void setUp() {
        group = new Group();
    }

    @Test
    public void construction() {
        assertNotNull(group);
        assertThat(group, not(equalTo(null)));
        assertThat(group.getMembers(), not(equalTo(null)));
        assertThat(group.getNames(), not(equalTo(null)));
        assertThat(group.getPath(), equalTo(""));
        assertThat(group.getMembers().size(), equalTo(0));
    }

    @Test
    public void accessors() {
        assertThat(group.getPath(), equalTo(""));

        group.setPath(null);
        assertThat(group.getPath(), equalTo(""));

        group.setPath("path");
        assertThat(group.getPath(), equalTo("path"));

        assertThat(group.getMembers().size(), equalTo(0));

        Subject subject0 = new Subject();
        subject0.setName("a");
        Subject subject1 = new Subject();
        subject1.setName("b");
        Subject subject2 = new Subject();
        subject2.setName("c");

        group.addMember(subject0);
        assertThat(group.getMembers().size(), equalTo(1));
        group.addMember(subject1);
        assertThat(group.getMembers().size(), equalTo(2));
        group.addMember(subject2);
        assertThat(group.getMembers().size(), equalTo(3));

        group.setMembers(null);
        assertThat(group.getMembers().size(), equalTo(0));
    }

    @Test
    public void equals() {
        Subject subject0 = new Subject();
        subject0.setName("Madonna");
        Subject subject1 = new Subject();
        subject1.setName("Prince");
        Subject subject2 = new Subject();
        subject2.setName("Archibald Cox");
        Subject subject3 = new Subject();
        subject3.setName("Leon Jaworski");
        Subject subject4 = new Subject();
        subject4.setName("Archibald Cox");
        Subject subject5 = new Subject();
        subject5.setName("Leon Jaworski");
        Subject subject6 = new Subject();
        subject6.setName("Tricky Dick");
        Subject subject7 = new Subject();
        subject7.setName("Richard Nixon");

        Group g0 = new Group();
        assertThat(g0, equalTo(g0));
        assertTrue(g0.equals(g0));
        assertFalse(g0.equals(null));
        assertFalse(g0.equals(new String()));

        Group g1 = new Group();
        assertThat(g0, equalTo(g1));
        assertThat(g1, equalTo(g0));

        g0.addMember(new Subject());
        assertThat(g0, not(equalTo(g1)));
        g1.addMember(new Subject());
        assertThat(g0, equalTo(g1));

        g0.addMember(subject0);
        assertThat(g0, not(equalTo(g1)));
        g1.addMember(subject0);
        assertThat(g0, equalTo(g1));

        g0.addMember(subject1);
        assertThat(g0, not(equalTo(g1)));
        g0.addMember(subject1);
        assertThat(g0, not(equalTo(g1)));

        g1.addMember(subject1);
        assertThat(g0, not(equalTo(g1)));
        g1.addMember(subject1);
        assertThat(g0, equalTo(g1));

        g0.setPath("path");
        assertThat(g0, not(equalTo(g1)));
        g1.setPath("path");
        assertThat(g0, equalTo(g1));

        g1.setPath("memo");
        assertThat(g1, not(equalTo(g0)));
        g0.setPath("memo");
        assertThat(g1, equalTo(g0));

        g1.setPath("memo");
        assertThat(g1, equalTo(g0));
        g0.setPath(null);
        assertThat(g1, not(equalTo(g0)));

        g0.setPath(null);
        assertThat(g0, not(equalTo(g1)));
        g1.setPath(null);
        assertThat(g0, equalTo(g1));

        assertThat(g0.getMembers().size(), equalTo(4));
        assertThat(g1.getMembers().size(), equalTo(4));

        g0.addMember(subject2);
        assertThat(g0, not(equalTo(g1)));
        g0.addMember(subject3);
        assertThat(g0, not(equalTo(g1)));
        g1.addMember(subject4);
        assertThat(g0, not(equalTo(g1)));
        g1.addMember(subject5);
        assertThat(g0, equalTo(g1));

        g0.addMember(subject6);
        g1.addMember(subject7);
        assertThat(g0, not(equalTo(g1)));

        g0 = new Group();
        assertFalse(g0.equals(null));
        assertFalse(g0.equals(new String()));
        assertTrue(g0.equals(g0));

        g1 = new Group();
        assertTrue(g0.equals(g1));
        assertTrue(g1.equals(g0));
        ReflectionTestUtils.setField(g0, "members", null);
        assertFalse(g0.equals(g1));
        assertFalse(g1.equals(g0));
        ReflectionTestUtils.setField(g1, "members", null);
        assertTrue(g0.equals(g1));
        assertTrue(g1.equals(g0));
        ReflectionTestUtils.setField(g0, "path", null);
        assertFalse(g0.equals(g1));
        assertFalse(g1.equals(g0));
        ReflectionTestUtils.setField(g1, "path", null);
        assertTrue(g0.equals(g1));
        assertTrue(g1.equals(g0));
    }

    @Test
    public void testHashCode() {
        Group g0 = new Group("a");

        int result = 1;
        final int prime = 31;
        int hashPath = g0.getPath().hashCode();
        assertThat(g0.getPath().hashCode(), equalTo("a".hashCode()));

        int hashMembers = g0.getMembers().hashCode();
        assertThat(hashMembers, equalTo(result));

        int hashCode = 1089;

        assertNotNull(g0.getPath());
        assertNotNull(g0.getMembers());

        assertThat(g0.hashCode(), equalTo(1089));

        ReflectionTestUtils.setField(g0, "path", null);
        assertThat(g0.hashCode(), equalTo(hashCode - hashPath));

        ReflectionTestUtils.setField(g0, "members", null);

        result = 1;
        result = prime * result + 0;
        result = prime * result + 0;
        assertThat(g0.hashCode(), equalTo(prime * prime));
    }

    @Test
    public void testToString() {
        assertThat(group.toString(), equalTo("Group [path=, members=[]]"));

        group = new Group("eno");
        assertThat(group.toString(), equalTo("Group [path=eno, members=[]]"));

        group = new Group("fripp", new ArrayList<Subject>());
        assertThat(group.toString(), equalTo("Group [path=fripp, members=[]]"));

        List<Subject> subjects = new ArrayList<>();
        Subject subject = new Subject("C", "A", "B");
        subjects.add(subject);
        group = new Group("manzanera", subjects);
        String expected = "Group [path=manzanera, "
                + "members=[Subject [name=A, uhUuid=B, uid=C]]]";
        assertThat(group.toString(), equalTo(expected));
    }

    @Test
    public void isEmptyTest() {
        group = new Group();
        assertTrue(group.isEmpty());
        Subject subject = new Subject("C", "A", "B");
        group.addMember(subject);
        assertFalse(group.isEmpty());
    }

}
