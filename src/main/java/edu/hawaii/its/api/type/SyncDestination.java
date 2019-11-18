package edu.hawaii.its.api.type;

public class SyncDestination {
    // 2 variables for object
    private String name;

    // Within description
    private String description;

    // Default Constructor
    public SyncDestination() {
        //empty
    }

    public SyncDestination(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
