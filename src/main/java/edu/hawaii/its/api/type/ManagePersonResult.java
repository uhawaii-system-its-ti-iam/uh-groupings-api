package edu.hawaii.its.api.type;

public class ManagePersonResult {
    private String path;
    private String name;
    private boolean inInclude = false;
    private boolean inExclude = false;
    private boolean inOwner = false;
    private boolean inBasisAndInclude = false;

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
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

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "Membership{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", inInclude=" + inInclude +
                ", inExclude=" + inExclude +
                ", inOwner=" + inOwner +
                ", inBasisAndInclude=" + inBasisAndInclude +
                '}';
    }
}
