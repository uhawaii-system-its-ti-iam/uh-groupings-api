package edu.hawaii.its.api.type;

import javax.persistence.*;

@Entity
public class Membership implements Comparable<Membership> {

    @Id
    @GeneratedValue
    @Column
    private String id;

    @ManyToOne
    private Person person;

    @ManyToOne
    private Group group;

    @Column
    private boolean selfOpted = false;

    public Membership() {

    }

    public Membership(Person person, Group group) {
        this.person = person;
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public boolean getSelfOpted() {
        return selfOpted;
    }

    public Person getPerson() {
        return person;
    }

    public Group getGroup() {
        return group;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setSelfOpted(boolean selfOpted) {
        this.selfOpted = selfOpted;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Membership) && (compareTo((Membership) o) == 0);
    }

    @Override
    public int compareTo(Membership membership) {
        int idComp = getId().compareTo(membership.getId());
        int personComp = getPerson().compareTo(membership.getPerson());
        int groupComp = getGroup().compareTo(membership.getGroup());

        if (idComp != 0) {
            return idComp;
        }
        if (personComp != 0) {
            return personComp;
        }
        if (groupComp != 0) {
            return groupComp;
        }
        return 0;
    }
}
