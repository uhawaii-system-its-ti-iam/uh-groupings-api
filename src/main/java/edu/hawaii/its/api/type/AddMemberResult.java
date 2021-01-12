package edu.hawaii.its.api.type;

public class AddMemberResult {
    boolean userWasAdded;
    boolean userWasRemoved;
    String pathOfAdd;
    String pathOfRemoved;
    String name;
    String uhUuid;
    String uid;

    public AddMemberResult() {
    }

    public AddMemberResult(boolean userWasAdded, boolean userWasRemoved, String pathOfAdd, String pathOfRemoved,
            String name, String uhUuid, String uid) {
        this.userWasAdded = userWasAdded;
        this.userWasRemoved = userWasRemoved;
        this.pathOfAdd = pathOfAdd;
        this.pathOfRemoved = pathOfRemoved;
        this.name = name;
        this.uhUuid = uhUuid;
        this.uid = uid;
    }

    public boolean isUserWasAdded() {
        return userWasAdded;
    }

    public boolean isUserWasRemoved() {
        return userWasRemoved;
    }

    public String getPathOfAdd() {
        return pathOfAdd;
    }

    public void setPathOfAdd(String pathOfAdd) {
        this.pathOfAdd = pathOfAdd;
    }

    public String getPathOfRemoved() {
        return pathOfRemoved;
    }

    public void setPathOfRemoved(String pathOfRemoved) {
        this.pathOfRemoved = pathOfRemoved;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUhUuid() {
        return uhUuid;
    }

    public void setUhUuid(String uhUuid) {
        this.uhUuid = uhUuid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override public String toString() {
        return "AddMemberResult{" +
                "userWasAdded=" + userWasAdded +
                ", userWasRemoved=" + userWasRemoved +
                ", pathOfAdd='" + pathOfAdd + '\'' +
                ", pathOfRemoved='" + pathOfRemoved + '\'' +
                ", name='" + name + '\'' +
                ", uhUuid='" + uhUuid + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
