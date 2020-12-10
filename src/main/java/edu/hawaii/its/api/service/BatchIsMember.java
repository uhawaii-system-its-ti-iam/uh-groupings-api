package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Person;

public interface BatchIsMember {
    boolean isMember(String groupPath, Person personToAdd);
}
