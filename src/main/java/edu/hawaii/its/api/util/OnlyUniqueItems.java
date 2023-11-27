package edu.hawaii.its.api.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class OnlyUniqueItems extends TypeSafeMatcher<List<String>> {
    @Override
    protected boolean matchesSafely(List<String> strings) {
        boolean onlyUniqueItems = true;
        Set<String> set = new HashSet<>();
        for (String string : strings) {
            if (!set.add(string)) {
                onlyUniqueItems = false;
                break;
            }
        }
        return onlyUniqueItems;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("only unique items");
    }

    public static Matcher<List<String>> onlyUniqueItems() {
        return new OnlyUniqueItems();
    }
}
