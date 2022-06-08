package edu.hawaii.its.api.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@WebAppConfiguration
public class GroupRepositoryTest {
    Person[] persons = new Person[10];
    Group[] groups = new Group[5];
    List<List<Person>> memberLists = new ArrayList<>();

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    public void setup() {
        for (int i = 0; i < 10; i++) {
            persons[i] = new Person("name" + i, "uuid" + i, "username" + i);
            personRepository.save(persons[i]);
        }

        for (int i = 0; i < 5; i++) {
            memberLists.add(new ArrayList<>());
        }

        memberLists.get(1).add(persons[0]);
        memberLists.get(1).add(persons[1]);

        memberLists.get(2).add(persons[2]);
        memberLists.get(2).add(persons[3]);
        memberLists.get(2).add(persons[4]);

        memberLists.get(3).add(persons[5]);
        memberLists.get(3).add(persons[6]);
        memberLists.get(3).add(persons[7]);
        memberLists.get(3).add(persons[8]);

        memberLists.get(4).add(persons[0]);
        memberLists.get(4).add(persons[1]);
        memberLists.get(4).add(persons[2]);
        memberLists.get(4).add(persons[3]);
        memberLists.get(4).add(persons[4]);
        memberLists.get(4).add(persons[5]);
        memberLists.get(4).add(persons[6]);
        memberLists.get(4).add(persons[7]);
        memberLists.get(4).add(persons[8]);
        memberLists.get(4).add(persons[9]);

        for (int i = 0; i < 5; i++) {
            groups[i] = new Group("path:to:group" + i, memberLists.get(i));
            groupRepository.save(groups[i]);
        }
    }

    @Test
    public void notNullTest() {
        Iterable<Person> personList = personRepository.findAll();
        Iterable<Group> groupList = groupRepository.findAll();

        assertNotNull(personList);
        assertNotNull(groupList);
    }

}
