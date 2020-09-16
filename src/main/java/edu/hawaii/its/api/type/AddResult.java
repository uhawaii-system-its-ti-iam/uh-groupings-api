package edu.hawaii.its.api.type;

public class AddResult {
    GroupPath addPath;
    GroupPath delPath;
    Boolean wasMoved;
    Person person;

    public AddResult(GroupPath addPath, GroupPath delPath, Person person) {
        this.addPath = addPath;
        this.delPath = delPath;
        this.person = person;
        if (null != delPath) {
            this.wasMoved = true;
        }
    }

    /* Getters */
    public Boolean getWasMoved() {
        return wasMoved;
    }

    public GroupPath getAddPath() {
        return addPath;
    }

    public GroupPath getDelPath() {
        return delPath;
    }

    public Person getPerson() {
        return person;
    }

    /* Setters */
    public void setAddPath(GroupPath addPath) {
        this.addPath = addPath;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setDelPath(GroupPath delPath) {
        this.delPath = delPath;
    }

    public void setWasMoved(Boolean wasMoved) {
        this.wasMoved = wasMoved;
    }
}