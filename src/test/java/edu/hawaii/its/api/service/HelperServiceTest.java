package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.repository.GroupRepository;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.repository.MembershipRepository;
import edu.hawaii.its.api.repository.PersonRepository;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.Person;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembership;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotNull;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class HelperServiceTest {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.basis_plus_include}")
    private String BASIS_PLUS_INCLUDE;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.test.username}")
    private String USERNAME;

    @Value("${groupings.api.test.name}")
    private String NAME;

    @Value("${groupings.api.test.uhuuid}")
    private String UHUUID;

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID_KEY;

    @Value("${groupings.api.person_attributes.username}")
    private String UID_KEY;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME_KEY;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME_KEY;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME_KEY;

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String INCLUDE = ":include";
    private static final String EXCLUDE = ":exclude";
    private static final String OWNERS = ":owners";
    private static final String BASIS = ":basis";

    private static final String GROUPING_2_PATH = PATH_ROOT + 2;

    private static final String GROUPING_2_INCLUDE_PATH = GROUPING_2_PATH + INCLUDE;
    private static final String GROUPING_2_EXCLUDE_PATH = GROUPING_2_PATH + EXCLUDE;
    private static final String GROUPING_2_BASIS_PATH = GROUPING_2_PATH + BASIS;
    private static final String GROUPING_2_OWNERS_PATH = GROUPING_2_PATH + OWNERS;

    private static final String ADMIN_USER = "admin";
    private static final Person ADMIN_PERSON = new Person(ADMIN_USER, ADMIN_USER, ADMIN_USER);
    private List<Person> admins = new ArrayList<>();
    private Group adminGroup = new Group();

    private static final String APP_USER = "app";
    private static final Person APP_PERSON = new Person(APP_USER, APP_USER, APP_USER);
    private List<Person> apps = new ArrayList<>();
    private Group appGroup = new Group();

    private List<Person> users = new ArrayList<>();
    private List<WsSubjectLookup> lookups = new ArrayList<>();

    @Autowired
    private GroupingRepository groupingRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private HelperService helperService;

    @Autowired
    private DatabaseSetupService databaseSetupService;

    @Before
    public void setup() {
        databaseSetupService.initialize(users, lookups, admins, adminGroup, appGroup);
    }

    @Test
    public void construction() {
        //autowired
        assertNotNull(helperService);
    }

    @Test
    public void parentGroupingPathTest() {
        assertThat(helperService.parentGroupingPath(GROUPING_2_BASIS_PATH), is(GROUPING_2_PATH));
        assertThat(helperService.parentGroupingPath(GROUPING_2_PATH + BASIS_PLUS_INCLUDE), is(GROUPING_2_PATH));
        assertThat(helperService.parentGroupingPath(GROUPING_2_EXCLUDE_PATH), is(GROUPING_2_PATH));
        assertThat(helperService.parentGroupingPath(GROUPING_2_INCLUDE_PATH), is(GROUPING_2_PATH));
        assertThat(helperService.parentGroupingPath(GROUPING_2_OWNERS_PATH), is(GROUPING_2_PATH));
        assertThat(helperService.parentGroupingPath(GROUPING_APPS), is(GROUPING_APPS));
        assertThat(helperService.parentGroupingPath(null), is(""));
    }

    /////////////////////////////////////////////////////
    // non-mocked tests//////////////////////////////////
    /////////////////////////////////////////////////////

    @Test
    public void toStringTest() {
        String helperString = helperService.toString();
        System.out.println(helperString);
        assertThat("HelperServiceImpl [SETTINGS=uh-settings]", is(helperString));
    }

    @Test
    public void groupingParentPath() {
        String grouping = "grouping";

        String[] groups = new String[] { grouping + EXCLUDE,
                grouping + INCLUDE,
                grouping + OWNERS,
                grouping + BASIS,
                grouping + BASIS_PLUS_INCLUDE,
                grouping };

        for (String g : groups) {
            assertThat(helperService.parentGroupingPath(g), is(grouping));
        }

        assertThat(helperService.parentGroupingPath(null), is(""));
    }

    @Test
    public void extractFirstMembershipID() {
        WsGetMembershipsResults mr = new WsGetMembershipsResults();
        WsMembership[] memberships = new WsMembership[3];
        for (int i = 0; i < 3; i++) {
            memberships[i] = new WsMembership();
            memberships[i].setMembershipId("membershipID_" + i);
        }
        mr.setWsMemberships(memberships);

        assertThat(helperService.extractFirstMembershipID(mr), is("membershipID_0"));
    }

    @Test
    public void makeGroupingsNoAttributes() {
        List<String> groupPaths = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            groupPaths.add("grouping_" + i);
        }
        for (int i = 0; i < 5; i++) {
            groupPaths.add("path:grouping_" + (i + 5));
        }

        List<Grouping> groupings = helperService.makeGroupings(groupPaths);

        for (int i = 5; i < 10; i++) {
            assertThat(groupings.get(i).getPath(), is("path:grouping_" + i));
            assertThat(groupings.get(i).getName(), is("grouping_" + i));
        }
    }

    @Test
    public void makeGroupingsServiceResultTest() {
        String action = "add a member";
        String resultCode = "successfully added member";
        WsAddMemberResults gr = new WsAddMemberResults();
        WsResultMeta resultMeta = new WsResultMeta();
        resultMeta.setResultCode(resultCode);
        gr.setResultMetadata(resultMeta);

        GroupingsServiceResult gsr = helperService.makeGroupingsServiceResult(gr, action);
        assertThat(gsr.getAction(), is(action));
        assertThat(gsr.getResultCode(), is(resultCode));

        resultMeta.setResultCode(FAILURE);
        gr.setResultMetadata(resultMeta);

        try {
            gsr = helperService.makeGroupingsServiceResult(gr, action);
            assertThat(gsr.getAction(), is(action));
            assertThat(gsr.getResultCode(), is(resultCode));
        } catch (GroupingsServiceResultException gsre) {
            gsre.printStackTrace();
        }

    }

    @Test
    public void extractFirstMembershipIDTest() {
        WsGetMembershipsResults membershipsResults = null;
        String firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertThat("", is(firstMembershipId));

        membershipsResults = new WsGetMembershipsResults();
        firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertThat("", is(firstMembershipId));

        WsMembership[] memberships = null;
        membershipsResults.setWsMemberships(memberships);
        firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertThat("", is(firstMembershipId));

        memberships = new WsMembership[] { null };
        membershipsResults.setWsMemberships(memberships);
        firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertThat("", is(firstMembershipId));

        WsMembership membership = new WsMembership();
        memberships = new WsMembership[] { membership };
        membershipsResults.setWsMemberships(memberships);
        firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertThat("", is(firstMembershipId));

        membership.setMembershipId("1234");
        memberships = new WsMembership[] { membership };
        membershipsResults.setWsMemberships(memberships);
        firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertThat("1234", is(firstMembershipId));
    }
}

