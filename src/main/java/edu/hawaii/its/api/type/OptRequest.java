package edu.hawaii.its.api.type;

import java.util.Objects;

public final class OptRequest {

    private final String groupNameRoot;
    private final GroupType groupType;
    private final Boolean optValue;
    private final String optId;
    private final PrivilegeType privilegeType;
    private final String uid;

    private OptRequest(OptType optType,
            GroupType groupType,
            Boolean optValue,
            String groupNameRoot,
            String uid,
            PrivilegeType privilegeType) {
        this.optId = optType.value();
        this.groupType = groupType;
        this.optValue = optValue;
        this.groupNameRoot = groupNameRoot;
        this.uid = uid;
        this.privilegeType = privilegeType;
    }

    public String getOptId() {
        return optId;
    }

    public Boolean getOptValue() {
        return optValue;
    }

    public String getGroupNameRoot() {
        return groupNameRoot;
    }

    public String getGroupName() {
        return groupNameRoot + groupType.value();
    }

    public PrivilegeType getPrivilegeType() {
        return privilegeType;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(optId, optValue, groupNameRoot, privilegeType, uid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OptRequest other = (OptRequest) obj;
        return Objects.equals(optId, other.optId)
                && Objects.equals(optValue, other.optValue)
                && Objects.equals(groupNameRoot, other.groupNameRoot)
                && Objects.equals(privilegeType, other.privilegeType)
                && Objects.equals(uid, other.uid);
    }

    public static class Builder {

        private String groupNameRoot;
        private GroupType groupType;
        private OptType optType;
        private Boolean optValue;
        private PrivilegeType privilegeType;
        private String uid;

        public Builder withOptType(OptType optType) {
            this.optType = optType;
            return this;
        }

        public Builder withOptValue(Boolean optValue) {
            this.optValue = optValue;
            return this;
        }

        public Builder withGroupNameRoot(String groupNameRoot) {
            this.groupNameRoot = groupNameRoot;
            return this;
        }

        public Builder withPrivilegeType(PrivilegeType privilegeType) {
            this.privilegeType = privilegeType;
            return this;
        }

        public Builder withUid(String uid) {
            this.uid = uid;
            return this;
        }

        public OptRequest build() {
            Objects.requireNonNull(optType, "optType cannot be null.");
            Objects.requireNonNull(optValue, "optValue cannot be null.");
            Objects.requireNonNull(groupNameRoot, "groupNameRoot cannot be null.");
            Objects.requireNonNull(uid, "uid cannot be null.");
            Objects.requireNonNull(privilegeType, "privilege cannot be null.");

            if (privilegeType.inclusionType() == optType.inclusionType()) {
                // PrivilegeType.OUT and OptType.OUT should produce GroupType.INCLUDE
                groupType = GroupType.INCLUDE;
            } else {
                groupType = GroupType.EXCLUDE;
            }

            return new OptRequest(optType, groupType, optValue, groupNameRoot, uid, privilegeType);
        }
    }
}
