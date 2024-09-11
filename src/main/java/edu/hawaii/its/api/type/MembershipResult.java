package edu.hawaii.its.api.type;

public class MembershipResult {

    private String path;
    private String name;
    private String description;
    private boolean isOptOutEnabled = false;

    public MembershipResult() {
        path = null;
        name = null;
        description = null;
    }

    public MembershipResult(String groupingPath, String groupingName, String groupingDescription) {
        path = groupingPath;
        name = groupingName;
        description = groupingDescription;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOptOutEnabled() {
        return isOptOutEnabled;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOptOutEnabled(boolean isOptOutEnabled) {
        this.isOptOutEnabled = isOptOutEnabled;
    }

    @Override
    public String toString() {
        return "Membership{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isOptOutEnabled=" + isOptOutEnabled +
                '}';
    }
}
