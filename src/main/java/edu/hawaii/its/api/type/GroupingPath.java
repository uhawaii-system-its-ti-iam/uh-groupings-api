package edu.hawaii.its.api.type;

public class GroupingPath {
    String path;
    String name;

    public GroupingPath(String path) {
        this.path = path;
        this.name = makeName();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String toString() {
        return "path: " + path + "; " +
                "name: " + name + ";";
    }

    private String makeName() {
        return path.substring(path.lastIndexOf(":") + 1);
    }
}
