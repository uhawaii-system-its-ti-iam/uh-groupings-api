package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Person;

public interface BatchIsMember {
    boolean isMember(String groupPath, Person personToAdd);
    boolean isOwner(String groupPath, String personToAdd);
    boolean isSuperuser(String username);
}
