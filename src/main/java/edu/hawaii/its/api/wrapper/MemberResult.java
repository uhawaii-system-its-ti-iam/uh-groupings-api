package edu.hawaii.its.api.wrapper;

public abstract class MemberResult extends Results implements Resultable {

    protected Subject subject;

    public String getUhUuid() {
        return subject.getUhUuid();
    }

    public String getName() {
        return subject.getName();
    }
}
