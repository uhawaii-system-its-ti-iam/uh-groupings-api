package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommandFactoryTest {

    @Test
    public void testCreateCommand(){

        CommandFactory commandFactory = new CommandFactory();

        assertEquals("A", commandFactory.create("returnA").execute());
        assertEquals("Z", commandFactory.create("returnZ").execute());
        assertEquals("B", commandFactory.create("returnB").execute());
        assertEquals("No such command exists", commandFactory.create("").execute());

    }

}