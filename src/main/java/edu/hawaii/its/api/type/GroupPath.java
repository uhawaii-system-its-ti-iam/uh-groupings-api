package edu.hawaii.its.api.type;

/**
 * A GroupPath is the path of a sub group within a grouping(Include, Exclude, Owners etc...) Creating a GroupPath object using path
 * "tmp:grouping-name:include", will result in parentPath="tmp:grouping-name", name="grouping-name." Passing a parent path into a
 * GroupPath at instantiation will result in undefined behavior.
 */
public class GroupPath {
    String path;
    String parentPath;
    String name;

    public GroupPath(String path) {
        this.path = path;
        this.parentPath = makeParentPath();
        this.name = makeName();
    }

    public String getName() {
        return name;
    }

    public String getParentPath() {
        return parentPath;
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String toString() {
        return "path: " + path + "; " +
                "parentPath: " + parentPath + "; " +
                "name: " + name + ";";
    }

    private String makeParentPath() {
        return path.substring(0, path.lastIndexOf(":"));
    }

    private String makeName() {
        return parentPath.substring(parentPath.lastIndexOf(":") + 1);
    }
}