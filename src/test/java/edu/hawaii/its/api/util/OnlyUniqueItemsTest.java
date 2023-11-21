package edu.hawaii.its.api.util;

import static edu.hawaii.its.api.util.OnlyUniqueItems.onlyUniqueItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

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

    @Test
    public void describeToTest() {
        OnlyUniqueItems onlyUniqueItems = new OnlyUniqueItems();
        Description description = new StringDescription();
        onlyUniqueItems.describeTo(description);
        assertThat(description.toString(), equalTo("only unique items"));
    }
}
