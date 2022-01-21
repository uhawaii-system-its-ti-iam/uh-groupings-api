package edu.hawaii.its.api.type;

public class AddMemberResult {
    private boolean userWasAdded;
    private boolean userWasRemoved;
    private String pathOfAdd;
    private String pathOfRemoved;
    private String name;
    private String uhUuid;
    private String uid;
    private String result;
    private String userIdentifier;

    public AddMemberResult() {
    }

    public AddMemberResult(boolean userWasAdded, boolean userWasRemoved, String pathOfAdd, String pathOfRemoved,
            String name, String uhUuid, String uid, String result, String userIdentifier) {
        this.userWasAdded = userWasAdded;
        this.userWasRemoved = userWasRemoved;
        this.pathOfAdd = pathOfAdd;
        this.pathOfRemoved = pathOfRemoved;
        this.name = name;
        this.uhUuid = uhUuid;
        this.uid = uid;
        this.result = result;
        this.userIdentifier = userIdentifier;
    }

    public AddMemberResult(boolean userWasAdded, String pathOfAdd,
            String name, String uhUuid, String uid, String result, String userIdentifier) {
        this.userWasAdded = userWasAdded;
        this.pathOfAdd = pathOfAdd;
        this.name = name;
        this.uhUuid = uhUuid;
        this.uid = uid;
        this.result = result;
        this.userIdentifier = userIdentifier;
    }

    public AddMemberResult(String userIdentifier, String result) {
        this.result = result;
        this.userIdentifier = userIdentifier;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    @Override
    public String toString() {
        return "AddMemberResult{" +
                "userWasAdded=" + userWasAdded +
                ", userWasRemoved=" + userWasRemoved +
                ", pathOfAdd='" + pathOfAdd + '\'' +
                ", pathOfRemoved='" + pathOfRemoved + '\'' +
                ", name='" + name + '\'' +
                ", uhUuid='" + uhUuid + '\'' +
                ", uid='" + uid + '\'' +
                ", result='" + result + '\'' +
                ", userIdentifier='" + userIdentifier + '\'' +
                '}';
    }

    public String[] toCsv() {
        String[] data = new String[4];
        data[0] = getUid();
        data[1] = getUhUuid();
        data[2] = getName();
        data[3] = getResult();
        return data;
    }
}