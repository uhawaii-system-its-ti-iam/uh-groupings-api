package edu.hawaii.its.api.type;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MembershipAssignment {
    private List<Grouping> groupingsIn;
    private List<Grouping> combinedGroupings;
    private List<Grouping> groupingsToOptInTo;
    private Map<String, Boolean> inBasis = new HashMap<>();
    private Map<String, Boolean> inInclude = new HashMap<>();
    private Map<String, Boolean> inExclude = new HashMap<>();
    private Map<String, Boolean> inOwner = new HashMap<>();

    public List<Grouping> getGroupingsIn() {
        return groupingsIn;
    }

    public void setGroupingsIn(List<Grouping> groupingsIn) {
        this.groupingsIn = groupingsIn;
    }

    public List<Grouping> getCombinedGroupings() {
        return combinedGroupings;
    }

    public void setCombinedGroupings(List<Grouping> combinedGroupings) {
        this.combinedGroupings = combinedGroupings;
    }

    public List<Grouping> getGroupingsToOptInTo() {
        return groupingsToOptInTo;
    }

    public void setGroupingsToOptInTo(List<Grouping> groupingsToOptInTo) {
        this.groupingsToOptInTo = groupingsToOptInTo;
    }

    public void addInBasis(String key, Boolean value) {
        inBasis.put(key, value);
    }

    public boolean isInBasis(String groupName) {
        return inBasis.get(groupName);
    }


    public Map<String, Boolean> getInBasis() {
        return inBasis;
    }

    public void addInInclude(String key, Boolean value) {
        inInclude.put(key, value);
    }

    public boolean isInInclude(String groupName) {
        return inInclude.get(groupName);
    }


    public Map<String, Boolean> getInInclude() {
        return inInclude;
    }

    public void addInExclude(String key, Boolean value) {
        inExclude.put(key, value);
    }

    public boolean isInExclude(String groupName) {
        return inExclude.get(groupName);
    }

    public Map<String, Boolean> getInExclude() {
        return inExclude;
    }

    public Map<String, Boolean> getInOwner() {
        return inOwner;
    }

    public void addInOwner(String key, Boolean value) { inOwner.put(key,value); }

    public boolean isInOwner(String key) {
        return inOwner.get(key);
    }
}
