package edu.hawaii.its.api.type;

import java.util.Map;

public class MemberAttributesResult {
    private GroupingsServiceResult result;
    private Map<String, String> attributes;

    // Constructor.
    public MemberAttributesResult() {
    }

    // Constructor.
    public MemberAttributesResult(GroupingsServiceResult result, Map<String, String> attributes) {
        this.result = result;
        this.attributes = attributes;
    }

    public void setResult(GroupingsServiceResult result) {
        this.result = result;
    }

    public GroupingsServiceResult getResult() {
        return result;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
