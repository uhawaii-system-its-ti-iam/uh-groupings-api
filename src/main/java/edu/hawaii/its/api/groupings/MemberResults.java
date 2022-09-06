package edu.hawaii.its.api.groupings;

import java.util.List;

@FunctionalInterface
public interface MemberResults<T> {
    List<T> getResults();
}
