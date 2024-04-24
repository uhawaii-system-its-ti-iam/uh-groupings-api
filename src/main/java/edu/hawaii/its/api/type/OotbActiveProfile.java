package edu.hawaii.its.api.type;

import java.util.List;

public class OotbActiveProfile {

    private final String uid;
    private final String uhUuid;
    private final String name;
    private final String givenName;
    private final List<String> authorities;

    OotbActiveProfile(Builder builder) {
        this.uid = builder.uid;
        this.uhUuid = builder.uhUuid;
        this.name = builder.name;
        this.givenName = builder.givenName;
        this.authorities = builder.authorities;
    }

    public String getUid() {
        return uid;
    }

    public String getUhUuid() {
        return uhUuid;
    }

    public String getName() {
        return name;
    }

    public String getGivenName() {
        return givenName;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public static class Builder {
        private String uid;
        private String uhUuid;
        private String name;
        private String givenName;
        private List<String> authorities;

        public Builder uid(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder uhUuid(String uhUuid) {
            this.uhUuid = uhUuid;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder givenName(String givenName) {
            this.givenName = givenName;
            return this;
        }

        public Builder authorities(List<String> authorities) {
            this.authorities = authorities;
            return this;
        }

        public OotbActiveProfile build() {
            return new OotbActiveProfile(this);
        }
    }
}
