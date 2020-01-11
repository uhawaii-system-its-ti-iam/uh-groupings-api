package edu.hawaii.its.api.type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Entity
@Table(name = "syncDestinations")
public class SyncDestination {
    // 3 variables for object
    private String name;
    private String description;
    private String tooltip;
    private Boolean isSynced;

    // Default Constructor
    public SyncDestination() {
        //empty
    }

    public SyncDestination(String name, String description) {
        this.name = name != null ? name : "";
        this.description = description != null ? description : "";
        this.tooltip = null;
        this.isSynced = null;
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

    public String parseKeyVal(String replace, String desc) {
        final String regex = "(\\$\\{)(.*)(})";
        String result;

        try {
             result = desc.replaceFirst(regex, replace);
        } catch(PatternSyntaxException e) {
            result = desc;
            e.printStackTrace();
        }


        return result;
    }
}
