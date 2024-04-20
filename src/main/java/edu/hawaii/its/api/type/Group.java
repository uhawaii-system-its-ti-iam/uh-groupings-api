package edu.hawaii.its.api.type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.hawaii.its.api.wrapper.Subject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {

    private List<Subject> members = new ArrayList<>();
    private String path = "";

    // Constructor.
    public Group() {
        // Empty.
    }

    // Constructor.
    public Group(List<Subject> members) {
        setMembers(members);
    }

    // Constructor.
    public Group(String path) {
        setPath(path);
    }

    // Constructor.
    public Group(String path, List<Subject> members) {
        this(members);
        setPath(path);
    }

    public void setPath(String path) {
        this.path = path != null ? path : "";
    }

    public String getPath() {
        return path;
    }

    public List<Subject> getMembers() {
        return members;
    }

    public void setMembers(List<Subject> members) {
        this.members = members != null ? members : new ArrayList<>();
    }

    public void addMember(Subject subject) {
        members.add(subject);
    }

    public List<String> getNames() {
        return members
                .parallelStream()
                .map(Subject::getName)
                .collect(Collectors.toList());
    }

    public List<String> getUhUuids() {
        return members
                .parallelStream()
                .map(Subject::getUhUuid)
                .collect(Collectors.toList());
    }

    public List<String> getUids() {
        return members
                .parallelStream()
                .map(Subject::getUid)
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
    public String toString() {
        return "Group [path=" + path + ", members=" + members + "]";
    }

    public boolean isEmpty() {
        return this.members.isEmpty();
    }

}
