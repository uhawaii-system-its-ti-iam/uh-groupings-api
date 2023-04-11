package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FindAttributesCommandTest {
    @Test
    public void constructor() {
        FindAttributesCommand findAttributesCommand = new FindAttributesCommand();
        assertNotNull(findAttributesCommand);
    }

    @Test
    public void builders() {
        FindAttributesCommand findAttributesCommand = new FindAttributesCommand();
        assertNotNull(findAttributesCommand.assignAttributeName(""));
        assertNotNull(findAttributesCommand.assignSearchScope(""));
    }
}
