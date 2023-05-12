package edu.hawaii.its.api.groupings;

public class GroupingSyncDestination {
    private String name;
    private String description;
    private String tooltip;
    private Boolean synced;
    private Boolean hidden;

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public Boolean getSynced() {
        return synced;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }
}
