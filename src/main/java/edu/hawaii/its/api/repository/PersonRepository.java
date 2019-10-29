package edu.hawaii.its.api.repository;

import edu.hawaii.its.api.type.Person;

import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, String> {
    Person findByUuid(String uuid);
    Person findByUsername(String username);
}
