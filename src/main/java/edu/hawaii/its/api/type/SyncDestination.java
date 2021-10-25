package edu.hawaii.its.api.type;

import java.util.regex.PatternSyntaxException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "syncDestinations")
public class SyncDestination {
    private String name;
    private String description;
    private String tooltip;
    private Boolean synced;
    private Boolean hidden;

    public SyncDestination() {
        this("", "");
    }

    public SyncDestination(String name, String description) {
        setName(name);
        setDescription(description);
        this.tooltip = "";
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

    /**
     * Replace ${} with replace in desc otherwise return desc.
     */
    @JsonIgnore
    @Transient
    public static String parseKeyVal(String replace, String desc) {
        final String regex = "(\\$\\{)(.*)(})";
        String result;

        try {
            result = desc.replaceFirst(regex, replace);
        } catch (PatternSyntaxException e) {
            result = desc;
        }

        return result;
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
