package edu.hawaii.its.api.type;

import edu.hawaii.its.api.wrapper.Subject;

public final class EmptyGroup extends Group {

    @Override
    public void addMember(Subject subject) {
        throw new UnsupportedOperationException();
    }
}
