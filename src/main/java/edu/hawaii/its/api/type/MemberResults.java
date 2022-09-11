package edu.hawaii.its.api.type;

import java.util.List;

@FunctionalInterface
public interface MemberResults<T> {
    List<T> getResults();
}
