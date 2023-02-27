package edu.hawaii.its.api.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ServiceTest {

    protected boolean containsDuplicates(List<String> list) {
        Set<String> set = new HashSet<>();
        boolean containsDuplicates = false;
        for (String str : list) {
            if (set.add(str)) {
                continue;
            }
            containsDuplicates = true;
        }
        return containsDuplicates;
    }
}
