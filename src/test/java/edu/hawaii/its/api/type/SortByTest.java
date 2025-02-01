package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SortByTest {

    @Test
    public void testSortByEnumValues(){
        SortBy[] expectedValues = { SortBy.NAME, SortBy.UID, SortBy.UH_UUID };
        assertArrayEquals(expectedValues, SortBy.values());
    }

    @Test
    public void testSortByName() {
        assertEquals(SortBy.NAME, SortBy.valueOf("NAME"));
    }

    @Test
    public void testSortByUID() {
        assertEquals(SortBy.UID, SortBy.valueOf("UID"));
    }

    @Test
    public void testSortByUhUuid() {
        assertEquals(SortBy.UH_UUID, SortBy.valueOf("UH_UUID"));
    }
    @Test
    public void testSortBySortString() {
        assertEquals("name", SortBy.NAME.sortString());
        assertEquals("search_string0", SortBy.UID.sortString());
        assertEquals("subjectId", SortBy.UH_UUID.sortString());
    }

    @Test
    public void testSortByValueOfWithNull() {
        assertThrows(NullPointerException.class, () -> {
            SortBy.valueOf((String) null);
        });
    }

    @Test
    public void testSortByValueWithInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> {
            SortBy.valueOf("INVALID_STRING");
        });
    }

}
