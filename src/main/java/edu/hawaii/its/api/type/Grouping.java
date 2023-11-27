package edu.hawaii.its.api.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Grouping {

    private String path;
    private String name;
    private String description;
    private Group basis;
    private Group exclude;
    private Group include;
    private Group composite;
    private Group owners;

    private boolean isEmpty;
    private boolean isOptInOn = false;
    private boolean isOptOutOn = false;

    // Constructor.
    public Grouping() {
        this("");
    }

    // Constructor.
    public Grouping(String path) {
        setPath(path);
        setDescription("");
        setBasis(new EmptyGroup());
        setExclude(new EmptyGroup());
        setInclude(new EmptyGroup());
        setComposite(new EmptyGroup());
        setOwners(new EmptyGroup());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path != null ? path : "";
        this.name = this.path;
        int index = this.name.lastIndexOf(':');
        if (index != -1) {
            this.name = this.name.substring(index + 1);
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public Group getBasis() {
        return basis;
    }

    public void setBasis(Group basis) {
        this.basis = basis != null ? basis : new EmptyGroup();
    }

    public Group getExclude() {
        return exclude;
    }

    public void setExclude(Group exclude) {
        this.exclude = exclude != null ? exclude : new EmptyGroup();
    }

    public Group getInclude() {
        return include;
    }

    public void setInclude(Group include) {
        this.include = include != null ? include : new EmptyGroup();
    }

    public Group getComposite() {
        return composite;
    }

    public void setComposite(Group composite) {
        this.composite = composite != null ? composite : new EmptyGroup();
    }

    public Group getOwners() {
        return owners;
    }

    public void setOwners(Group owners) {
        this.owners = owners != null ? owners : new EmptyGroup();
    }

    public boolean isOptInOn() {
        return isOptInOn;
    }

    public void setOptInOn(boolean isOptInOn) {
        this.isOptInOn = isOptInOn;
    }

    public boolean isOptOutOn() {
        return isOptOutOn;
    }

    public void setOptOutOn(boolean isOptOutOn) {
        this.isOptOutOn = isOptOutOn;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    /* Set the isEmpty field to true if all the groups are empty. */
    public void setIsEmpty() {
        this.isEmpty = this.getComposite().isEmpty() &&
                this.getBasis().isEmpty() &&
                this.getInclude().isEmpty() &&
                this.getExclude().isEmpty() &&
                this.getOwners().isEmpty();
    }
}
