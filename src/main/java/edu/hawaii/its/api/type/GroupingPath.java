package edu.hawaii.its.api.type;

import edu.hawaii.its.api.util.Strings;

public class GroupingPath {
    private String path;
    private String name;
    private String description;
    private static final String NO_DESCRIPTION_TEXT = "No description given for this Grouping.";

    // Constructor
    public GroupingPath() {
    }

    public GroupingPath(String path) {
        this.path = path;
        setName();
        this.description = "No description given for this Grouping.";
    }

    public GroupingPath(String path, String description) {
        this.path = path;
        setName();
        this.description = Strings.isEmpty(description) ? NO_DESCRIPTION_TEXT : description;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return "path: " + path + "; " +
                "name: " + name + "; " +
                "description: " + description + ";";
    }

    private void setName() {
        name = path.substring(path.lastIndexOf(":") + 1);
    }

}
