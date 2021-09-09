package edu.hawaii.its.api.type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "syncDestinations")
public class SyncDestination {
    // 3 variables for object
    private String name;
    private String description;
    private String tooltip;
    private Boolean synced;
    private Boolean hidden;

    // Default Constructor
    public SyncDestination() {
        this.hidden = false;
        this.synced = false;
    }

    public SyncDestination(String name, String description) {
        this.name = name != null ? name : "";
        this.description = description != null ? description : "";
        this.hidden = false;
        this.synced = false;
    }

    @Id
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    @Column(name = "tooltip")
    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
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

    @Override public String toString() {
        return "SyncDestination[" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tooltip='" + tooltip + '\'' +
                ", synced=" + synced +
                ", hidden=" + hidden +
                ']';
    }
}
