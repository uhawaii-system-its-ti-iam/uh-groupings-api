package edu.hawaii.its.api.type;

import java.util.List;

public class MembersAttributesResult {
    private List<Person> membersAttributes;
    private String result;

    public MembersAttributesResult() {
    }

    public MembersAttributesResult(List<Person> membersAttributes, String result) {
        this.membersAttributes = membersAttributes;
        this.result = result;
    }

    public List<Person> getMembersAttributes() {
        return membersAttributes;
    }

    public void setMembersAttributes(List<Person> membersAttributes) {
        this.membersAttributes = membersAttributes;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
