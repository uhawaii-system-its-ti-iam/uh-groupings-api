package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommandFactoryTest {

    @Test
    public void testCreateCommand(){

        CommandFactory commandFactory = new CommandFactory();

        assertEquals("A", commandFactory.create("aCommand").execute());
        assertEquals("Z", commandFactory.create("zCommand").execute());
        assertEquals("B", commandFactory.create("bCommand").execute());
        assertEquals("No such command exists", commandFactory.create("").execute());
        assertThrows(ArithmeticException.class, () -> commandFactory.create("arithmeticExceptionCommand").execute(), "arithmeticExceptionCommand should throw a ArithmeticException");
        assertThrows(NullPointerException.class, () -> commandFactory.create("nullPointerExceptionCommand").execute(), "nullPointerExceptionCommand should throw a NullPointerException");


    }

}