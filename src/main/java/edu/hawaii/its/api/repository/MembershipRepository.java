package edu.hawaii.its.api.repository;

import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;

import org.springframework.data.repository.CrudRepository;

public interface MembershipRepository extends CrudRepository<Membership, String> {

    Membership findByIdentifier(String identifier);

    Membership findByPersonAndGroup(Person person, Group group);
}
