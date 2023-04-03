package edu.hawaii.its.api.type;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Group implements Comparable<Group> {

    private List<Person> members = new ArrayList<>();
    private String path = "";

    // Constructor.
    public Group() {
        // Empty.
    }

    // Constructor.
    public Group(List<Person> members) {
        setMembers(members);
    }

    // Constructor.
    public Group(String path) {
        setPath(path);
    }

    // Constructor.
    public Group(String path, List<Person> members) {
        this(members);
        setPath(path);
    }

    public void setPath(String path) {
        this.path = path != null ? path : "";
    }

    public String getPath() {
        return path;
    }

    public List<Person> getMembers() {
        return members;
    }

    public void setMembers(List<Person> members) {
        this.members = members != null ? members : new ArrayList<>();
    }

    public void addMember(Person person) {
        members.add(person);
    }

    @JsonIgnore
    public boolean isMember(Person person) {
        return members.contains(person);
    }

    @JsonIgnore
    public List<String> getNames() {
        return members
                .parallelStream()
                .map(Person::getName)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> getUhUuids() {
        return members
                .parallelStream()
                .map(Person::getUhUuid)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> getUsernames() {
        return members
                .parallelStream()
                .map(Person::getUsername)
                .collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((members == null) ? 0 : members.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Group other = (Group) obj;
        if (members == null) {
            if (other.members != null)
                return false;
        } else if (!members.equals(other.members))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

    @Override
    public int compareTo(Group group) {
        int pathComp = getPath().compareTo(group.getPath());
        if (pathComp != 0) {
            return pathComp;
        }
        int size0 = getMembers().size();
        int size1 = group.getMembers().size();
        if (size0 != size1) {
            return Integer.compare(size0, size1);
        }

        for (int i = 0; i < size0; i++) {
            Person p0 = getMembers().get(i);
            Person p1 = group.getMembers().get(i);
            int personComp = p0.compareTo(p1);
            if (personComp != 0) {
                return personComp;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return "Group [path=" + path + ", members=" + members + "]";
    }

    public boolean isEmpty() {
        return this.members.isEmpty();
    }

}
