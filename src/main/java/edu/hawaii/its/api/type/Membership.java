package edu.hawaii.its.api.type;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class Membership  {

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
    private String path;

    @Column
    private String name;

    @Column
    private boolean isSelfOpted = false;

    @Column
    private boolean isOptInEnabled = false;

    @Column
    private boolean isOptOutEnabled = false;

    @Column
    private boolean inBasis = false;

    @Column
    private boolean inInclude = false;

    @Column
    private boolean inExclude = false;

    @Column
    private boolean inOwner = false;

    @Column
    private boolean inBasisAndInclude = false;

    public Membership() {

    }

    public Membership(Person person, Group group) {
        this.person = person;
        this.group = group;
    }

    public Person getPerson() {
        return person;
    }

    public Group getGroup() {
        return group;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return path;
    }

    public boolean isInBasis() {
        return inBasis;
    }

    public boolean isInOwner() {
        return inOwner;
    }

    public boolean isInInclude() {
        return inInclude;
    }

    public boolean isInBasisAndInclude() {
        return inBasisAndInclude;
    }

    public boolean isInExclude() {
        return inExclude;
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

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setInBasis(boolean inBasis) {
        this.inBasis = inBasis;
    }

    public void setInOwner(boolean inOwner) {
        this.inOwner = inOwner;
    }

    public void setInInclude(boolean inInclude) {
        this.inInclude = inInclude;
    }

    public void setInExclude(boolean inExclude) {
        this.inExclude = inExclude;
    }

    public void setInBasisAndInclude(boolean inBasisAndInclude) {
        this.inBasisAndInclude = inBasisAndInclude;
    }

    @Override public String toString() {
        return "Membership{" +
                "identifier='" + identifier + '\'' +
                ", person=" + person +
                ", group=" + group +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", isSelfOpted=" + isSelfOpted +
                ", isOptInEnabled=" + isOptInEnabled +
                ", isOptOutEnabled=" + isOptOutEnabled +
                ", inBasis=" + inBasis +
                ", inInclude=" + inInclude +
                ", inExclude=" + inExclude +
                ", inOwner=" + inOwner +
                ", inBasisAndInclude=" + inBasisAndInclude +
                '}';
    }
}
