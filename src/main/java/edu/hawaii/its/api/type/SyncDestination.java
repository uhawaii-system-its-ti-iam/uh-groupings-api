package edu.hawaii.its.api.type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "syncDestinations")
public class SyncDestination {
    // 2 variables for object
    private String name;
    private String description;

    // Default Constructor
    public SyncDestination() {
        //empty
    }

    public SyncDestination(String name, String description) {
        this.name = name;
        this.description = description;
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
}
