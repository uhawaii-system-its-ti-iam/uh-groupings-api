package edu.hawaii.its.api.groupings;

import com.fasterxml.jackson.annotation.JsonAlias;

public class GroupingSyncDestinationFeature {
    private String name;
    private String description;
    private String tooltip;
    private Boolean synced;
    private Boolean hidden;

    @JsonAlias("protected")
    private Boolean isProtected;

    @JsonAlias("protected-tooltip")
    private String protectedTooltip;

    @JsonAlias("disable-warning")
    private String disableWarning;

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

    public Boolean getIsProtected() {
        return isProtected;
    }

    public String getProtectedTooltip() {
        return protectedTooltip;
    }

    public String getDisableWarning() {
        return disableWarning;
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

    public void setIsProtected(Boolean isProtected) {
        this.isProtected = isProtected;
    }

    public void setProtectedTooltip(String protectedTooltip) {
        this.protectedTooltip = protectedTooltip;
    }

    public void setDisableWarning(String disableWarning) {
        this.disableWarning = disableWarning;
    }
}
