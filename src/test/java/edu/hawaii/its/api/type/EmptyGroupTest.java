package edu.hawaii.its.api.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmptyGroupTest extends Group {

    private EmptyGroup emptygroup;

    public ExpectedException thrown;

    @BeforeEach
    public void setup() {
        thrown = ExpectedException.none();
        emptygroup = new EmptyGroup();
    }

    @Test
    public void test() {
        assertThrows(UnsupportedOperationException.class, () -> emptygroup.addMember(new Person()));
    }
}