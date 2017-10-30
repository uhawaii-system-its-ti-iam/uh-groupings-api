package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MembershipRepository extends CrudRepository<Membership, Long> {
    List<Membership> findByPerson(Person person);

    List<Membership> findByPersonUsername(String username);

    List<Membership> findByGroup(Group group);

    Membership findById(Long id);

    Membership findByPersonAndGroup(Person person, Group group);
}
