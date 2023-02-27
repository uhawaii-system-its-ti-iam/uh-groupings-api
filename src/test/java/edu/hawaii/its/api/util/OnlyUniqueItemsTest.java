package edu.hawaii.its.api.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static edu.hawaii.its.api.util.OnlyUniqueItems.onlyUniqueItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class OnlyUniqueItemsTest {
    @Test
    public void onlyUniqueItemsTest() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        assertThat(list, is(onlyUniqueItems()));
        list.add("a");
        assertThat(list, not(onlyUniqueItems()));
    }
}
