package edu.hawaii.its.api.type;

public class ManageSubjectResult {
    private String path;
    private String name;
    private boolean inInclude = false;
    private boolean inExclude = false;
    private boolean inOwner = false;
    private boolean inBasisAndInclude = false;
    private boolean ownerGrouping = false;

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

    /**
     * Whether this grouping is used as the owner-grouping of some grouping, meaning its members are owners of that
     * grouping.
     */
    public boolean isOwnerGrouping() {
        return ownerGrouping;
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

    public void setOwnerGrouping(boolean ownerGrouping) {
        this.ownerGrouping = ownerGrouping;
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
                ", ownerGrouping=" + ownerGrouping +
                '}';
    }
}
