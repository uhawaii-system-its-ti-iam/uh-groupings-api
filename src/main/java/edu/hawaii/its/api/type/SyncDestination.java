package edu.hawaii.its.api.type;

public class SyncDestination {

    private String name;
    private String description;
    private String tooltip;
    private Boolean synced;
    private Boolean hidden;

    // Constructor.
    public SyncDestination() {
        this("", "");
    }

    // Constructor.
    public SyncDestination(String name, String description) {
        setName(name);
        setDescription(description);
        this.tooltip = "";
        this.hidden = false;
        this.synced = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip != null ? tooltip : "";
    }

    public Boolean isSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced != null && synced;
    }

    public Boolean isHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden != null && hidden;
    }

    @Override
    public String toString() {
        return "SyncDestination[" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tooltip='" + tooltip + '\'' +
                ", synced=" + synced +
                ", hidden=" + hidden +
                ']';
    }
}
