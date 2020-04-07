package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.GroupingsAPIConfig;
import edu.hawaii.its.api.repository.GroupRepository;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.repository.MembershipRepository;
import edu.hawaii.its.api.repository.PersonRepository;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service("databaseSetupService")
public class DatabaseSetupServiceImpl implements DatabaseSetupService {

    private static final String ADMIN_USER = "admin";
    private static final Person ADMIN_PERSON = new Person(ADMIN_USER, ADMIN_USER, ADMIN_USER);
    private List<Person> admins = new ArrayList<>();
    private Group adminGroup;

    private static final String APP_USER = "app";
    private static final Person APP_PERSON = new Person(APP_USER, APP_USER, APP_USER);
    private List<Person> apps = new ArrayList<>();
    private Group appGroup;

    private String pathRoot = "path:to:grouping";

    private List<Person> users = new ArrayList<>();
    private List<WsSubjectLookup> lookups = new ArrayList<>();
    private List<Person> persons = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();
    private List<Grouping> groupings = new ArrayList<>();

    @Autowired
    private GroupingsAPIConfig config;

    @Autowired
    private GroupingRepository groupingRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Override
    public void initialize(
            List<Person> users,
            List<WsSubjectLookup> lookups,
            List<Person> admins,
            Group adminGroup,
            Group appGroup) {
        this.users = users;
        this.lookups = lookups;
        this.admins = admins;
        this.adminGroup = adminGroup;
        this.appGroup = appGroup;

        fillDatabase();
        setUserLookups();
        setAdminAppUsers();
    }

    private void setUserLookups() {
        for (int i = 0; i < 100; i++) {
            String name = config.getNAME() + i;
            String uhUuid = String.valueOf(i);
            String username = config.getUSERNAME() + i;

            Person person = new Person(name, uhUuid, username);
            users.add(person);

            WsSubjectLookup lookup = new WsSubjectLookup(null, null, username);
            lookups.add(lookup);
        }
    }

    private void setAdminAppUsers() {

        admins.add(ADMIN_PERSON);
        adminGroup.setMembers(admins);
        adminGroup.setPath(config.getGROUPING_ADMINS());
        personRepository.save(ADMIN_PERSON);
        groupRepository.save(adminGroup);

        apps.add(APP_PERSON);
        appGroup.setMembers(apps);
        appGroup.setPath(config.getGROUPING_APPS());
        personRepository.save(APP_PERSON);
        groupRepository.save(appGroup);
    }

    private void fillDatabase() {
        fillPersonRepository();
        fillGroupRepository();
        fillGroupingRepository();

        setUpMemberships();
    }

    private void fillPersonRepository() {
        setUpPersons();

        personRepository.saveAll(persons);
    }

    private void fillGroupRepository() {
        setUpGroups();

        groupRepository.saveAll(groups);
    }

    private void fillGroupingRepository() {
        setUpGroupings();

        groupingRepository.saveAll(groupings);
    }

    /////////////////////////////////////////////////////
    // setup methods
    /////////////////////////////////////////////////////

    private void setUpPersons() {
        int numberOfPersons = 100;
        for (int i = 0; i < numberOfPersons; i++) {
            makePerson("name" + i, Integer.toString(i), "username" + i, "surnameOf" + i, "givenNameOf" + i);
        }
    }

    private void setUpGroups() {
        setUpGroup0();
        setUpGroup1();
        setUpGroup2();
        setUpGroup3();
        setUpGroup4();
    }

    private void setUpGroup(int i,
                            List<Person> basisMembers,
                            List<Person> excludeMembers,
                            List<Person> includeMembers,
                            List<Person> ownerMembers) {

        makeGroup(basisMembers, pathRoot + i + config.getBasis());
        makeGroup(excludeMembers, pathRoot + i + config.getEXCLUDE());
        makeGroup(includeMembers, pathRoot + i + config.getINCLUDE());
        makeGroup(ownerMembers, pathRoot + i + config.getOWNERS());

        // add all of the owners to the owner group
        Group ownerGroup = groupRepository.findByPath(config.getGROUPING_OWNERS());
        if (ownerGroup == null) {
            ownerGroup = new Group(config.getGROUPING_OWNERS());
        }
        ownerMembers.forEach(ownerGroup::addMember);
        groupRepository.save(ownerGroup);
    }

    private void setUpGroup0() {
        List<Person> basisMembers = new ArrayList<>();
        List<Person> excludeMembers = new ArrayList<>();
        List<Person> includeMembers = new ArrayList<>();
        List<Person> ownerMembers = new ArrayList<>();

        basisMembers.add(persons.get(0));
        basisMembers.add(persons.get(1));
        basisMembers.add(persons.get(2));
        basisMembers.add(persons.get(3));
        basisMembers.add(persons.get(4));

        excludeMembers.add(persons.get(2));
        excludeMembers.add(persons.get(3));
        excludeMembers.add(persons.get(4));

        includeMembers.add(persons.get(5));
        includeMembers.add(persons.get(6));
        includeMembers.add(persons.get(7));
        includeMembers.add(persons.get(8));
        includeMembers.add(persons.get(9));

        ownerMembers.add(persons.get(0));

        setUpGroup(0, basisMembers, excludeMembers, includeMembers, ownerMembers);
    }

    private void setUpGroup1() {
        List<Person> basisMembers = new ArrayList<>();
        List<Person> excludeMembers = new ArrayList<>();
        List<Person> includeMembers = new ArrayList<>();
        List<Person> ownerMembers = new ArrayList<>();

        basisMembers.add(persons.get(0));
        basisMembers.add(persons.get(1));
        basisMembers.add(persons.get(2));
        basisMembers.add(persons.get(3));
        basisMembers.add(persons.get(4));

        excludeMembers.add(persons.get(2));
        excludeMembers.add(persons.get(3));
        excludeMembers.add(persons.get(4));

        includeMembers.add(persons.get(5));
        includeMembers.add(persons.get(6));
        includeMembers.add(persons.get(7));
        includeMembers.add(persons.get(8));
        includeMembers.add(persons.get(9));

        ownerMembers.add(persons.get(0));

        setUpGroup(1, basisMembers, excludeMembers, includeMembers, ownerMembers);
    }

    private void setUpGroup2() {
        List<Person> basisMembers = new ArrayList<>();
        List<Person> excludeMembers = new ArrayList<>();
        List<Person> includeMembers = new ArrayList<>();
        List<Person> ownerMembers = new ArrayList<>();

        basisMembers.add(persons.get(0));
        basisMembers.add(persons.get(1));
        basisMembers.add(persons.get(2));
        basisMembers.add(persons.get(3));
        basisMembers.add(persons.get(4));

        excludeMembers.add(persons.get(2));
        excludeMembers.add(persons.get(3));
        excludeMembers.add(persons.get(4));

        includeMembers.add(persons.get(5));
        includeMembers.add(persons.get(6));
        includeMembers.add(persons.get(7));
        includeMembers.add(persons.get(8));
        includeMembers.add(persons.get(9));

        ownerMembers.add(persons.get(0));

        setUpGroup(2, basisMembers, excludeMembers, includeMembers, ownerMembers);
    }

    private void setUpGroup3() {
        List<Person> basisMembers = new ArrayList<>();
        List<Person> excludeMembers = new ArrayList<>();
        List<Person> includeMembers = new ArrayList<>();
        List<Person> ownerMembers = new ArrayList<>();

        basisMembers.add(persons.get(0));
        basisMembers.add(persons.get(1));
        basisMembers.add(persons.get(2));
        basisMembers.add(persons.get(3));
        basisMembers.add(persons.get(4));

        excludeMembers.add(persons.get(2));
        excludeMembers.add(persons.get(3));
        excludeMembers.add(persons.get(4));

        includeMembers.add(persons.get(5));
        includeMembers.add(persons.get(6));
        includeMembers.add(persons.get(7));
        includeMembers.add(persons.get(8));
        includeMembers.add(persons.get(9));

        ownerMembers.add(persons.get(0));

        setUpGroup(3, basisMembers, excludeMembers, includeMembers, ownerMembers);
    }

    private void setUpGroup4() {
        List<Person> basisMembers = new ArrayList<>();
        List<Person> excludeMembers = new ArrayList<>();
        List<Person> includeMembers = new ArrayList<>();
        List<Person> ownerMembers = new ArrayList<>();

        basisMembers.add(persons.get(0));
        basisMembers.add(persons.get(1));
        basisMembers.add(persons.get(2));
        basisMembers.add(persons.get(3));
        basisMembers.add(persons.get(4));

        excludeMembers.add(persons.get(2));
        excludeMembers.add(persons.get(3));
        excludeMembers.add(persons.get(4));

        includeMembers.add(persons.get(5));
        includeMembers.add(persons.get(6));
        includeMembers.add(persons.get(7));
        includeMembers.add(persons.get(8));
        includeMembers.add(persons.get(9));

        ownerMembers.add(persons.get(0));

        setUpGroup(4, basisMembers, excludeMembers, includeMembers, ownerMembers);
    }

    private void setUpGroupings() {
        makeGrouping(pathRoot + 0, groups.get(0), groups.get(1), groups.get(2), groups.get(3), false, true, false,
                true);
        makeGrouping(pathRoot + 1, groups.get(4), groups.get(5), groups.get(6), groups.get(7), false, true, true,
                false);
        makeGrouping(pathRoot + 2, groups.get(8), groups.get(9), groups.get(10), groups.get(11), true, false, false,
                false);
        makeGrouping(pathRoot + 3, groups.get(12), groups.get(13), groups.get(14), groups.get(15), true, true, true,
                false);
        makeGrouping(pathRoot + 4, groups.get(16), groups.get(17), groups.get(18), groups.get(19), false, false, false,
                false);
    }

    private void setUpMemberships() {
        Person grouperAll = new Person();
        grouperAll.setUsername("GrouperAll");
        personRepository.save(grouperAll);

        Iterable<Group> groups = groupRepository.findAll();

        for (Group group : groups) {
            group.addMember(grouperAll);
            groupRepository.save(group);
            for (Person person : group.getMembers()) {
                Membership membership = new Membership(person, group);
                membershipRepository.save(membership);
            }
        }

        Iterable<Grouping> groupings = groupingRepository.findAll();

        for (Grouping grouping : groupings) {
            Membership allExclude = membershipRepository.findByPersonAndGroup(grouperAll, grouping.getExclude());
            Membership allInclude = membershipRepository.findByPersonAndGroup(grouperAll, grouping.getInclude());
            Membership allComposite = membershipRepository.findByPersonAndGroup(grouperAll, grouping.getComposite());
            if (grouping.isOptOutOn()) {
                allComposite.setOptOutEnabled(true);
                allExclude.setOptInEnabled(true);
                allExclude.setOptOutEnabled(true);

            }
            if (grouping.isOptInOn()) {
                allComposite.setOptInEnabled(true);
                allInclude.setOptInEnabled(true);
                allInclude.setOptOutEnabled(true);
            }
            membershipRepository.save(allComposite);
            membershipRepository.save(allExclude);
            membershipRepository.save(allInclude);
        }
    }

    ///////////////////////////////////////////////////////////
    // factory methods
    ///////////////////////////////////////////////////////////

    private void makePerson(String name, String uhUuid, String username, String surname, String givenName) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cn", name);
        attributes.put("uhUuid", uhUuid);
        attributes.put("uid", username);
        attributes.put("sn", surname);
        attributes.put("givenName", givenName);
        persons.add(new Person(attributes));
    }

    private void makeGroup(List<Person> members, String path) {
        groups.add(new Group(path, members));
    }

    private void makeGrouping(
            String path,
            Group basis,
            Group exclude,
            Group include,
            Group owners,
            boolean isListserveOn,
            boolean isOptInOn,
            boolean isOptOutOn,
            boolean isReleasedGroupingOn) {

        Grouping grouping = new Grouping(path);
        Group composite = buildComposite(include, exclude, basis, path);
        groupRepository.save(composite);

        grouping.setBasis(basis);
        grouping.setExclude(exclude);
        grouping.setInclude(include);
        grouping.setOwners(owners);
        grouping.setComposite(composite);
        grouping.setDescription("");
        grouping.setSyncDestinations(buildSyncDestinations());

        grouping.changeSyncDestinationState(config.getLISTSERV(), isListserveOn);
        grouping.setOptInOn(isOptInOn);
        grouping.setOptOutOn(isOptOutOn);
        grouping.changeSyncDestinationState(config.getRELEASED_GROUPING(), isReleasedGroupingOn);

        groupings.add(grouping);
    }

    ///////////////////////////////////////////////////////////
    // helper methods
    ///////////////////////////////////////////////////////////

    private Group buildComposite(Group include, Group exclude, Group basis, String path) {
        Group basisPlusInclude = addIncludedMembers(include, basis);
        Group compositeGroup = removeExcludedMembers(basisPlusInclude, exclude);
        compositeGroup.setPath(path);

        return compositeGroup;
    }

    private Group addIncludedMembers(Group include, Group basis) {
        Set<Person> s = new TreeSet<>();
        s.addAll(include.getMembers());
        s.addAll(basis.getMembers());

        return new Group(new ArrayList<>(s));
    }


    /*
    * Builds the sync destinations for unit testing.
    *
    */

    private List<SyncDestination> buildSyncDestinations() {

        List<SyncDestination> syncDestinations = new ArrayList<>();

        syncDestinations.add(new SyncDestination(config.getLISTSERV(), "listserv"));
        syncDestinations.add(new SyncDestination(config.getRELEASED_GROUPING(), "releasedGrouping"));
        syncDestinations.add(new SyncDestination(config.getGOOGLE_GROUP(), "google-group"));

        return syncDestinations;
    }

    private Group removeExcludedMembers(Group basisPlusInclude, Group exclude) {
        List<Person> newBasisPlusInclude = new ArrayList<>(basisPlusInclude.getMembers());
        newBasisPlusInclude.removeAll(exclude.getMembers());

        Group basisPlusIncludeMinusExcludeGroup = new Group();
        basisPlusIncludeMinusExcludeGroup.setMembers(newBasisPlusInclude);

        return basisPlusIncludeMinusExcludeGroup;
    }
}
