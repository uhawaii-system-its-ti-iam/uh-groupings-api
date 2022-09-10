package edu.hawaii.its.api.wrapper;

public abstract class MemberResult extends Results implements Resultable {

    protected Subject subject;

    protected String getUhUuid() {
        return subject.getUhUuid();
    }
    protected String getName() {
        return subject.getName();
    }
}
