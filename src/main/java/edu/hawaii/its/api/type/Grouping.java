package edu.hawaii.its.api.type;


import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "grouping")
public class Grouping {

    @Id
    @Column(name = "path")
    private String path;

    @Column
    private String name;

    @Column
    private String description;

    @OneToOne
    private Group basis;

    @OneToOne
    private Group exclude;

    @OneToOne
    private Group include;

    @OneToOne
    private Group composite;

    @OneToOne
    private Group owners;

    @ElementCollection
    private Map<String, Boolean> syncDestinations = new HashMap<>();

    @Column
    private boolean isListservOn = false;

    @Column
    private boolean isOptInOn = false;

    @Column
    private boolean isOptOutOn = false;

    @Column
    private boolean isReleasedGroupingOn = false;

    // Constructor.
    public Grouping() {
        this("");
    }

    // Constructor.
    public Grouping(String path) {
        setPath(path);

        setDescription("");
        setBasis(new EmptyGroup());
        setExclude(new EmptyGroup());
        setInclude(new EmptyGroup());
        setComposite(new EmptyGroup());
        setOwners(new EmptyGroup());
    }

//    @JsonIgnore
    @ElementCollection
    public Map<String, Boolean> getSyncDestinations() {
        return syncDestinations;
    }

    public void setSyncDestinations(Map<String, Boolean> syncDestinations) {
        this.syncDestinations = syncDestinations;
    }

    @Transient
    public boolean isSyncDestinationOn(String key) {
        if (!syncDestinations.containsKey(key)) {
            return false;
        }
        return syncDestinations.get(key);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path != null ? path : "";
        this.name = this.path;
        int index = this.name.lastIndexOf(':');
        if (index != -1) {
            this.name = this.name.substring(index + 1);
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public Group getBasis() {
        return basis;
    }

    public void setBasis(Group basis) {
        this.basis = basis != null ? basis : new EmptyGroup();
    }

    public Group getExclude() {
        return exclude;
    }

    public void setExclude(Group exclude) {
        this.exclude = exclude != null ? exclude : new EmptyGroup();
    }

    public Group getInclude() {
        return include;
    }

    public void setInclude(Group include) {
        this.include = include != null ? include : new EmptyGroup();
    }

    public Group getComposite() {
        return composite;
    }

    public void setComposite(Group composite) {
        this.composite = composite != null ? composite : new EmptyGroup();
    }

    public Group getOwners() {
        return owners;
    }

    public void setOwners(Group owners) {
        this.owners = owners != null ? owners : new EmptyGroup();
    }

    public boolean isListservOn() {
        return isListservOn;
    }

    public void setListservOn(boolean isListservOn) {
        this.isListservOn = isListservOn;
    }

    public boolean isOptInOn() {
        return isOptInOn;
    }

    public void setOptInOn(boolean isOptInOn) {
        this.isOptInOn = isOptInOn;
    }

    public boolean isOptOutOn() {
        return isOptOutOn;
    }

    public void setOptOutOn(boolean isOptOutOn) {
        this.isOptOutOn = isOptOutOn;
    }

    public boolean isReleasedGroupingOn() {
        return isReleasedGroupingOn;
    }

    public void setReleasedGroupingOn(boolean isReleasedGroupingOn) {
        this.isReleasedGroupingOn = isReleasedGroupingOn;
    }

    @Override
    public String toString() {
        return "Grouping [name=" + name
                + ", path=" + path
                + ", ListservOn=" + isListservOn()
                + ", OptInOn=" + isOptInOn()
                + ", OptOutOn=" + isOptOutOn()
                + ", basis=" + basis
                + ", owners=" + owners
                + "]";
    }
}
