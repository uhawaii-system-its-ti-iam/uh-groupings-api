package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class OptRequestTest {

    @Test
    public void optTypeInIn() {
        OptRequest optRequest = new OptRequest.Builder()
                .withUid("yoda")
                .withGroupNameRoot("t:yoda:yoda-aux")
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        assertEquals(optRequest, optRequest);
        assertThat(optRequest.getOptId(), equalTo(OptType.IN.value()));
        assertThat(optRequest.getGroupName(), equalTo("t:yoda:yoda-aux:include"));
        assertThat(optRequest.getGroupNameRoot(), equalTo("t:yoda:yoda-aux"));
    }

    @Test
    public void optTypeInOut() {
        OptRequest optRequest = new OptRequest.Builder()
                .withUid("yoda")
                .withGroupNameRoot("t:yoda:yoda-aux")
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        assertEquals(optRequest, optRequest);
        assertEquals(optRequest.getOptId(), OptType.OUT.value());
        assertThat(optRequest.getOptId(), equalTo(OptType.OUT.value()));
        assertThat(optRequest.getGroupName(), equalTo("t:yoda:yoda-aux:exclude"));
        assertThat(optRequest.getGroupNameRoot(), equalTo("t:yoda:yoda-aux"));
        assertThat(optRequest.getOptValue(), notNullValue());
        assertThat(optRequest.getPrivilegeType(), notNullValue());
        assertThat(optRequest.getUid(), notNullValue());
    }

    @Test
    public void optTypeOutIn() {
        OptRequest optRequest = new OptRequest.Builder()
                .withUid("yoda")
                .withGroupNameRoot("t:yoda:yoda-aux")
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        assertEquals(optRequest, optRequest);
        assertThat(optRequest.getOptId(), equalTo(OptType.IN.value()));
        assertThat(optRequest.getGroupName(), equalTo("t:yoda:yoda-aux:exclude"));
        assertThat(optRequest.getGroupNameRoot(), equalTo("t:yoda:yoda-aux"));
    }

    @Test
    public void optTypeOutOut() {
        OptRequest optRequest = new OptRequest.Builder()
                .withUid("yoda")
                .withGroupNameRoot("t:yoda:yoda-aux")
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        assertEquals(optRequest, optRequest);
        assertThat(optRequest.getOptId(), equalTo(OptType.OUT.value()));
        assertThat(optRequest.getGroupName(), equalTo("t:yoda:yoda-aux:include"));
        assertThat(optRequest.getGroupNameRoot(), equalTo("t:yoda:yoda-aux"));
    }

    @Test
    public void build() {
        Exception exception = assertThrows(NullPointerException.class,
                () -> new OptRequest.Builder()
                        .build());
        String expectedMessage = "optType cannot be null.";
        String actualMessage = exception.getMessage();
        assertThat(actualMessage, equalTo(expectedMessage));

        exception = assertThrows(NullPointerException.class,
                () -> new OptRequest.Builder()
                        .withOptType(OptType.OUT)
                        .build());
        expectedMessage = "optValue cannot be null.";
        actualMessage = exception.getMessage();
        assertThat(actualMessage, equalTo(expectedMessage));

        exception = assertThrows(NullPointerException.class,
                () -> new OptRequest.Builder()
                        .withOptType(OptType.OUT)
                        .withOptValue(Boolean.TRUE)
                        .build());
        expectedMessage = "groupNameRoot cannot be null.";
        actualMessage = exception.getMessage();
        assertThat(actualMessage, equalTo(expectedMessage));

        exception = assertThrows(NullPointerException.class,
                () -> new OptRequest.Builder()
                        .withOptType(OptType.OUT)
                        .withOptValue(Boolean.TRUE)
                        .withGroupNameRoot("some:root")
                        .build());
        expectedMessage = "uid cannot be null.";
        actualMessage = exception.getMessage();
        assertThat(actualMessage, equalTo(expectedMessage));

        exception = assertThrows(NullPointerException.class,
                () -> new OptRequest.Builder()
                        .withOptType(OptType.OUT)
                        .withOptValue(Boolean.TRUE)
                        .withGroupNameRoot("some:root")
                        .withUid("hansolo")
                        .build());
        expectedMessage = "privilege cannot be null.";
        actualMessage = exception.getMessage();
        assertThat(actualMessage, equalTo(expectedMessage));

        OptRequest optRequest = new OptRequest.Builder()
                .withOptType(OptType.OUT)
                .withOptValue(Boolean.TRUE)
                .withGroupNameRoot("some:root")
                .withUid("hansolo")
                .withPrivilegeType(PrivilegeType.IN)
                .build();
        assertThat(optRequest, notNullValue());

        optRequest = new OptRequest.Builder()
                .withOptType(OptType.OUT)
                .withOptValue(Boolean.TRUE)
                .withGroupNameRoot("some:root")
                .withUid("hansolo")
                .withPrivilegeType(PrivilegeType.OUT)
                .build();
        assertThat(optRequest, notNullValue());
    }

    @Test
    public void testHashCode() {
        OptRequest optRequestOne = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withOptValue(true)
                .withGroupNameRoot("some-path")
                .withPrivilegeType(PrivilegeType.IN)
                .withUid("uid")
                .build();
        assertEquals(optRequestOne.hashCode(), optRequestOne.hashCode());

        OptRequest optRequestTwo = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withOptValue(true)
                .withGroupNameRoot("some-path")
                .withPrivilegeType(PrivilegeType.IN)
                .withUid("uid")
                .build();
        assertEquals(optRequestTwo.hashCode(), optRequestTwo.hashCode());

        assertEquals(optRequestOne.hashCode(), optRequestTwo.hashCode());
    }

    @Test
    public void testToEquals() {
        OptRequest optRequestOne = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withOptValue(true)
                .withGroupNameRoot("some-path")
                .withPrivilegeType(PrivilegeType.IN)
                .withUid("uid")
                .build();
        assertEquals(optRequestOne, optRequestOne);
        assertTrue(optRequestOne.equals(optRequestOne));

        OptRequest optRequestTwo = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withOptValue(true)
                .withGroupNameRoot("some-path")
                .withPrivilegeType(PrivilegeType.IN)
                .withUid("uid")
                .build();
        assertEquals(optRequestTwo, optRequestTwo);
        assertTrue(optRequestTwo.equals(optRequestTwo));

        assertEquals(optRequestOne, optRequestTwo);
        assertTrue(optRequestOne.equals(optRequestTwo));

        // Some falses.
        optRequestTwo = new OptRequest.Builder()
                .withOptType(OptType.OUT)
                .withOptValue(true)
                .withGroupNameRoot("some-path")
                .withPrivilegeType(PrivilegeType.IN)
                .withUid("uid")
                .build();
        assertThat(optRequestOne, not(equalTo(optRequestTwo)));
        assertFalse(optRequestOne.equals(optRequestTwo));
        assertThat(optRequestOne.hashCode(), not(equalTo(optRequestTwo.hashCode())));

        optRequestTwo = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withOptValue(false)
                .withGroupNameRoot("some-path")
                .withPrivilegeType(PrivilegeType.IN)
                .withUid("uid")
                .build();
        assertThat(optRequestOne, not(equalTo(optRequestTwo)));
        assertFalse(optRequestOne.equals(optRequestTwo));
        assertThat(optRequestOne.hashCode(), not(equalTo(optRequestTwo.hashCode())));

        optRequestTwo = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withOptValue(true)
                .withGroupNameRoot("some-pathX")
                .withPrivilegeType(PrivilegeType.IN)
                .withUid("uid")
                .build();
        assertThat(optRequestOne, not(equalTo(optRequestTwo)));
        assertFalse(optRequestOne.equals(optRequestTwo));
        assertThat(optRequestOne.hashCode(), not(equalTo(optRequestTwo.hashCode())));

        optRequestTwo = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withOptValue(true)
                .withGroupNameRoot("some-path")
                .withPrivilegeType(PrivilegeType.OUT)
                .withUid("uid")
                .build();
        assertThat(optRequestOne, not(equalTo(optRequestTwo)));
        assertFalse(optRequestOne.equals(optRequestTwo));
        assertThat(optRequestOne.hashCode(), not(equalTo(optRequestTwo.hashCode())));

        optRequestTwo = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withOptValue(true)
                .withGroupNameRoot("some-path")
                .withPrivilegeType(PrivilegeType.IN)
                .withUid("uidX")
                .build();
        assertThat(optRequestOne, not(equalTo(optRequestTwo)));
        assertFalse(optRequestOne.equals(optRequestTwo));
        assertThat(optRequestOne.hashCode(), not(equalTo(optRequestTwo.hashCode())));

        // Misc odd ball falses.
        assertFalse(optRequestOne == null);
        assertFalse(optRequestOne.equals("no-way-jose"));
    }
}
