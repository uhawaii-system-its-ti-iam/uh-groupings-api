package edu.hawaii.its.api.type;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class Membership implements Comparable<Membership> {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column
    private String identifier;

    @ManyToOne
    private Person person;

    @ManyToOne
    private Group group;

    @Column
    private boolean isSelfOpted = false;

    @Column
    private boolean isOptInEnabled = false;

    @Column
    private boolean isOptOutEnabled = false;

    private boolean inBasis = false;
    private boolean inInclude = false;

    public Membership() {

    }

    public Membership(Person person, Group group) {
        this.person = person;
        this.group = group;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Person getPerson() {
        return person;
    }

    public Group getGroup() {
        return group;
    }

    public boolean isSelfOpted() {
        return isSelfOpted;
    }

    public boolean isOptInEnabled() {
        return isOptInEnabled;
    }

    public boolean isOptOutEnabled() {
        return isOptOutEnabled;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setSelfOpted(boolean isSelfOpted) {
        this.isSelfOpted = isSelfOpted;
    }

    public void setOptInEnabled(boolean isOptInEnabled) {
        this.isOptInEnabled = isOptInEnabled;
    }

    public void setOptOutEnabled(boolean isOptOutEnabled) {
        this.isOptOutEnabled = isOptOutEnabled;
    }

    @Override
    public boolean equals(Object o) {
        return ((compareTo((Membership) o) == 0 && o instanceof Membership));
    }

    @Override
    public int compareTo(Membership membership) {
        if(membership != null) {
            int idComp = membership.getIdentifier() != null ? getIdentifier().compareTo(membership.getIdentifier()) : -1;
            int personComp = membership.getPerson() != null ? getPerson().compareTo(membership.getPerson()) : -1;
            int groupComp = membership.getGroup() != null ? getGroup().compareTo(membership.getGroup()) : -1;

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
        return -1;
    }
}
