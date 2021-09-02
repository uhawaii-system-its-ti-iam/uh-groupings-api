package edu.hawaii.its.api.type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.regex.PatternSyntaxException;

@Entity
@Table(name = "syncDestinations")
public class SyncDestination {
    // 3 variables for object
    private String name;
    private String description;
    private String tooltip;
    private Boolean isSynced;
    private Boolean hidden;

    // Default Constructor
    public SyncDestination() {
        this.hidden = false;
    }

    public SyncDestination(String name, String description) {
        this.name = name != null ? name : "";
        this.description = description != null ? description : "";
        this.tooltip = null;
        this.isSynced = null;
        this.hidden = false;
    }

    @Id
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "tooltip")
    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public Boolean getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(Boolean isSynced) {
        this.isSynced = isSynced;
    }

    public Boolean getHidden() {
        return this.hidden;
    }

    public void setHidden(Boolean hidden) {
        // Set this.hidden to false if hidden is null.
        this.hidden = Boolean.valueOf(String.valueOf(hidden));
    }

    public String parseKeyVal(String replace, String desc) {
        final String regex = "(\\$\\{)(.*)(})";
        String result;

        try {
            result = desc.replaceFirst(regex, replace);
        } catch (PatternSyntaxException e) {
            result = desc;
            e.printStackTrace();
        }

        return result;
    }

    @Override public String toString() {
        return "SyncDestination{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tooltip='" + tooltip + '\'' +
                ", isSynced=" + isSynced + '\'' +
                ", hidden=" + hidden +
                '}';
    }
}
