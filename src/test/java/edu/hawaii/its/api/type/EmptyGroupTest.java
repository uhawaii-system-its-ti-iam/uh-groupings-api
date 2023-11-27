package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EmptyGroupTest extends Group {

    private EmptyGroup emptygroup;

    @BeforeEach
    public void setup() {
        emptygroup = new EmptyGroup();
    }

    @Test
    public void test() {
        assertThrows(UnsupportedOperationException.class, () -> emptygroup.addMember(new Person()));
    }
}