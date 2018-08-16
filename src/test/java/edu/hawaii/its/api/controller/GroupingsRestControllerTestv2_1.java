package edu.hawaii.its.api.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.GroupingFactoryService;
import edu.hawaii.its.api.service.HelperService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.EmptyGroup;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingAssignment;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@RunWith(SpringRunner.class)
@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingsRestControllerTestv2_1 {

    @Value("${app.iam.request.form}")
    private String requestForm;

    @MockBean
    private GroupAttributeService groupAttributeService;

    @MockBean
    private GroupingAssignmentService groupingAssignmentService;

    @MockBean
    private GroupingFactoryService groupingFactoryService;

    @MockBean
    private HelperService helperService;

    @MockBean
    private MemberAttributeService memberAttributeService;

    @MockBean
    private MembershipService membershipService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // Test data.
    private Grouping grouping() {
        Grouping grouping = new Grouping("test:ing:me:bob");

        Group basisGroup = new Group();
        basisGroup.addMember(new Person("b0-name", "b0-uuid", "b0-username"));
        basisGroup.addMember(new Person("b1-name", "b1-uuid", "b1-username"));
        basisGroup.addMember(new Person("b2-name", "b2-uuid", "b2-username"));
        grouping.setBasis(basisGroup);

        Group exclude = new Group();
        exclude.addMember(new Person("e0-name", "e0-uuid", "e0-username"));
        grouping.setExclude(exclude);

        Group include = new Group();
        include.addMember(new Person("i0-name", "i0-uuid", "i0-username"));
        include.addMember(new Person("i1-name", "i1-uuid", "i1-username"));
        grouping.setInclude(include);

        Group owners = new Group();
        owners.addMember(new Person("o0-name", "o0-uuid", "o0-username"));
        owners.addMember(new Person("o1-name", "o1-uuid", "o1-username"));
        owners.addMember(new Person("o2-name", "o2-uuid", "o2-username"));
        owners.addMember(new Person("o3-name", "o3-uuid", "o3-username"));
        grouping.setOwners(owners);

        grouping.setListservOn(true);

        return grouping;
    }

    // Test data.
    private Grouping groupingTwo() {
        Grouping grouping = new Grouping("test:ing:me:kim");

        Group basisGroup = new Group();
        basisGroup.addMember(new Person("b4-name", "b4-uuid", "b4-username"));
        basisGroup.addMember(new Person("b5-name", "b5-uuid", "b5-username"));
        basisGroup.addMember(new Person("b6-name", "b6-uuid", "b6-username"));
        grouping.setBasis(basisGroup);

        Group exclude = new Group();
        exclude.addMember(new Person("e4-name", "e4-uuid", "e4-username"));
        grouping.setExclude(exclude);

        Group include = new Group();
        include.addMember(new Person("i4-name", "i4-uuid", "i4-username"));
        include.addMember(new Person("i5-name", "i5-uuid", "i5-username"));
        grouping.setInclude(include);

        Group owners = new Group();
        owners.addMember(new Person("o4-name", "o4-uuid", "o4-username"));
        owners.addMember(new Person("o5-name", "o5-uuid", "o5-username"));
        owners.addMember(new Person("o6-name", "o6-uuid", "o6-username"));
        owners.addMember(new Person("o7-name", "o7-uuid", "o7-username"));
        grouping.setOwners(owners);

        grouping.setListservOn(true);

        return grouping;
    }

    // Test data.
    private AdminListsHolder mockAdminListsHolder() {
        AdminListsHolder holder = new AdminListsHolder();
        List<Grouping> mockAllGroupings = new ArrayList<>();
        Group mockAdminGroup = new Group();

        mockAllGroupings.add(grouping());
        mockAllGroupings.add(groupingTwo());
        holder.setAllGroupings(mockAllGroupings);

        mockAdminGroup.addMember(new Person("o4-name", "o4-uuid", "o4-username"));
        mockAdminGroup.addMember(new Person("o5-name", "o5-uuid", "o5-username"));
        mockAdminGroup.addMember(new Person("o6-name", "o6-uuid", "o6-username"));
        mockAdminGroup.addMember(new Person("o7-name", "o7-uuid", "o7-username"));
        holder.setAdminGroup(mockAdminGroup);

        return holder;
    }

    // Test data (2.1 API).
    private List<String> groupingStringList() {
        List<String> mockGroupingList = new ArrayList<>();

        mockGroupingList.add("g0-gName");
        mockGroupingList.add("g1-gName");
        mockGroupingList.add("g2-gName");

        return mockGroupingList;
    }

    //Test data (2.1 API).
    private List<Grouping> groupingList() {
        GroupingAssignment mg = new GroupingAssignment();
        List<Grouping> groupings = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            groupings.add(grouping());
            groupings.get(i).setPath("grouping" + i);
        }

        return groupings;
    }

    //Test data (2.1 API).
    private List<GroupingsServiceResult> gsrList() {
        List<GroupingsServiceResult> gsrList= new ArrayList<>();
        gsrList.add(new GroupingsServiceResult("SUCCESS", "add users to grouping"));
        return gsrList;
    }

    //Test data (2.1 API).
    private List<GroupingsServiceResult> gsrList2() {
        List<GroupingsServiceResult> gsrList= new ArrayList<>();
        gsrList.add(new GroupingsServiceResult("SUCCESS", "delete member from include group"));
        return gsrList;
    }

    //Test data.
    private GroupingAssignment myGroupings() {
        GroupingAssignment mg = new GroupingAssignment();
        List<Grouping> groupings = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            groupings.add(grouping());
            groupings.get(i).setPath("grouping" + i);
        }

        mg.setGroupingsIn(groupings);
        mg.setGroupingsOwned(groupings);
        mg.setGroupingsOptedOutOf(groupings);
        mg.setGroupingsOptedInTo(groupings);
        mg.setGroupingsToOptOutOf(groupings);
        mg.setGroupingsToOptInTo(groupings);

        return mg;
    }

    //HiSTART!!!.
    //Hi.
    //Hi.
    //Hi.

    @Test
    @WithMockUhUser(username = "bobo")
    public void adminsGroupingsTest() throws Exception {
        final String admin = "bobo";

        given(groupingAssignmentService.adminLists("bobo")).willReturn(mockAdminListsHolder());

        //        AdminListsHolder holder = mockAdminListsHolder();
        //        System.out.println("*************LOOOOOOK HEEEERE!!!!: " + holder.getAdminGroup().getNames());


        // THE PROBLEM IS BELOW! (THE PERFORM(GET...) IS NOT RETURNING A VALID JSONPATH...) --> Problem was with the Principal arg given to adminsGroupings() in GroupingsRestController(), needed to match username in WithMockUhUser class, or, be overridden in the WithMockUhUser annotation above.
        mockMvc.perform(get("/api/groupings/v2.1/adminsGroupings"))
                .andExpect(status().isOk())        // NOTE: This will work if the urlTemplate above is valid.
                .andExpect(jsonPath("allGroupings[0].name").value("bob"))
                .andExpect(jsonPath("allGroupings[0].path").value("test:ing:me:bob"))
                .andExpect(jsonPath("allGroupings[0].listservOn").value("true"))
                .andExpect(jsonPath("allGroupings[0].basis.members", hasSize(3)))
                .andExpect(jsonPath("allGroupings[0].basis.members[0].name").value("b0-name"))
                .andExpect(jsonPath("allGroupings[0].basis.members[0].uuid").value("b0-uuid"))
                .andExpect(jsonPath("allGroupings[0].basis.members[0].username").value("b0-username"))
                .andExpect(jsonPath("allGroupings[0].basis.members[1].name").value("b1-name"))
                .andExpect(jsonPath("allGroupings[0].basis.members[1].username").value("b1-username"))
                .andExpect(jsonPath("allGroupings[0].basis.members[2].name").value("b2-name"))
                .andExpect(jsonPath("allGroupings[0].basis.members[2].uuid").value("b2-uuid"))
                .andExpect(jsonPath("allGroupings[0].basis.members[2].username").value("b2-username"))
                .andExpect(jsonPath("allGroupings[0].exclude.members", hasSize(1)))
                .andExpect(jsonPath("allGroupings[0].exclude.members[0].name").value("e0-name"))
                .andExpect(jsonPath("allGroupings[0].exclude.members[0].name").value("e0-name"))
                .andExpect(jsonPath("allGroupings[0].exclude.members[0].uuid").value("e0-uuid"))
                .andExpect(jsonPath("allGroupings[0].include.members", hasSize(2)))
                .andExpect(jsonPath("allGroupings[0].include.members[1].name").value("i1-name"))
                .andExpect(jsonPath("allGroupings[0].include.members[1].name").value("i1-name"))
                .andExpect(jsonPath("allGroupings[0].include.members[1].uuid").value("i1-uuid"))
                .andExpect(jsonPath("allGroupings[0].owners.members", hasSize(4)))
                .andExpect(jsonPath("allGroupings[0].owners.members[3].name").value("o3-name"))
                .andExpect(jsonPath("allGroupings[0].owners.members[3].uuid").value("o3-uuid"))
                .andExpect(jsonPath("allGroupings[0].owners.members[3].username").value("o3-username"))

                .andExpect(jsonPath("allGroupings[1].name").value("kim"))
                .andExpect(jsonPath("allGroupings[1].path").value("test:ing:me:kim"))
                .andExpect(jsonPath("allGroupings[1].listservOn").value("true"))
                .andExpect(jsonPath("allGroupings[1].basis.members", hasSize(3)))
                .andExpect(jsonPath("allGroupings[1].basis.members[0].name").value("b4-name"))
                .andExpect(jsonPath("allGroupings[1].basis.members[0].uuid").value("b4-uuid"))
                .andExpect(jsonPath("allGroupings[1].basis.members[0].username").value("b4-username"))
                .andExpect(jsonPath("allGroupings[1].basis.members[1].name").value("b5-name"))

                .andExpect(jsonPath("adminGroup.members[0].name").value("o4-name"))
                .andExpect(jsonPath("adminGroup.members[0].uuid").value("o4-uuid"))
                .andExpect(jsonPath("adminGroup.members[1].uuid").value("o5-uuid"))
                .andExpect(jsonPath("adminGroup.members[1].username").value("o5-username"))
                .andExpect(jsonPath("adminGroup.members[2].name").value("o6-name"))
                .andExpect(jsonPath("adminGroup.members[3].uuid").value("o7-uuid"))
                .andExpect(jsonPath("adminGroup.members", hasSize(4)));
    }

    @Test
    @WithMockUhUser
    public void memberGroupingsTest() throws Exception {
        final String uid = "grouping";

        given(helperService.extractGroupings(groupingAssignmentService.getGroupPaths(uid)))
                .willReturn(groupingStringList());

        mockMvc.perform(get("/api/groupings/v2.1/members/grouping/groupings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("g0-gName"))
                .andExpect(jsonPath("$[1]").value("g1-gName"))
                .andExpect(jsonPath("$[2]").value("g2-gName"));
    }


    // *********BELOW IS A TEST METHOD MADE AS AN EXAMPLE TO ENSURE ANONYMOUS USER CANNOT GET IN (Frank)
    @Test
    @WithAnonymousUser
    public void anonMemberGroupingsTest() throws Exception {

        mockMvc.perform(get("/api/groupings/v2.1/members/grouping/groupings"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUhUser
    public void ownerGroupingsTest() throws Exception {
        final String uid = "grouping";

        given(groupingAssignmentService.groupingsOwned(groupingAssignmentService.getGroupPaths(uid)))
                .willReturn(groupingList());

        mockMvc.perform(get("/api/groupings/v2.1/owners/grouping/groupings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("grouping0"))
                .andExpect(jsonPath("$[1].name").value("grouping1"))
                .andExpect(jsonPath("$[2].name").value("grouping2"))
                .andExpect(jsonPath("$[0].basis.members[0].name").value("b0-name"))
                .andExpect(jsonPath("$[0].basis.members[1].name").value("b1-name"))
                .andExpect(jsonPath("$[1].basis.members[0].name").value("b0-name"))
                .andExpect(jsonPath("$[2].basis.members[2].name").value("b2-name"))
                .andExpect(jsonPath("$[0].owners.members[0].uuid").value("o0-uuid"))
                .andExpect(jsonPath("$[1].owners.members[2].uuid").value("o2-uuid"));
    }

    /*
        @Test
    @WithMockUhUser(username = "admin")
    public void addAdminTest() throws Exception {
        given(membershipService.addAdmin("admin", "newAdmin"))
                .willReturn(new GroupingsServiceResult("SUCCESS", "add admin"));

        mockMvc.perform(post("/api/groupings/newAdmin/addAdmin")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("add admin"));
    }
     */

    @Test
    @WithMockUhUser(username = "uhAdmin")
    public void addNewAdminTest() throws Exception {
        given(membershipService.addAdmin("uhAdmin", "newAdmin"))
                .willReturn(new GroupingsServiceResult("SUCCESS", "add admin"));

        mockMvc.perform(post("/api/groupings/v2.1/admins/newAdmin").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("add admin"));
    }

    @Test
    @WithMockUhUser(username = "uhAdmin")       // later, check other admin usernames...
    public void addOwnerTest() throws Exception {
        //final String path = "grouping";

//        GroupingsServiceResult gsr = new GroupingsServiceResult("yea!", "an action.");
//        System.out.println(gsr.getResultCode());

        given(memberAttributeService.assignOwnership("path1", "uhAdmin", "newOwner"))
                .willReturn(new GroupingsServiceResult("SUCCESS", "give newOwner ownership of path1"));

        // PROBLEM: URL TEMPLATE IS INVALID, or, problem with mockMvc call?
        //----> perform(put(......!!!
        mockMvc.perform(put("/api/groupings/v2.1/groupings/path1/owners/newOwner").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("give newOwner ownership of path1"))
        //.andExpect(status().is4xxClientError())
        ;
    }

    @Test
    @WithMockUhUser
    public void includeMembersTest() throws Exception {

        given(membershipService.addGroupMember("user", "grouping:include", "tst04name"))
                .willReturn(gsrList());

        mockMvc.perform(put("/api/groupings/v2.1/groupings/grouping/includeMembers/tst04name").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("add users to grouping"))
        ;
    }

    @Test
    @WithMockUhUser
    public void excludeMembersTest() throws Exception {
        given(membershipService.addGroupMember("user", "grouping:exclude", "tst04name"))
                .willReturn(gsrList2());

        mockMvc.perform(put("/api/groupings/v2.1/groupings/grouping/excludeMembers/tst04name").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("delete member from include group"))
        ;
    }

    @Test
    @WithMockUhUser
    public void enablePreferenceTest() throws Exception {
        given(membershipService.addGroupMember("user", "grouping:exclude", "tst04name"))
                .willReturn(gsrList2());

        mockMvc.perform(put("/api/groupings/v2.1/groupings/grouping/excludeMembers/tst04name").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("delete member from include group"))
        ;
    }

/*
@RequestMapping(value = "/adminsGroupings",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<AdminListsHolder> adminsGroupings(Principal principal) {
        logger.info("Entered REST adminsGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.adminLists(principal.getName()));  }
 */

/*
    @Test
    @WithMockUhUser
    public void adminsGroupingsTest() throws Exception {
        final String admin = "admin";

        given(groupingAssignmentService.adminLists(admin)).willReturn(mockAdminListsHolder());

        mockMvc.perform(get("/api/groupings/v2.0/adminsGroupings"))
                .andExpect(status().isOk())        // NOTE: This will work if the urlTemplate above is valid.
                //.andExpect(jsonPath("allGroupings[0].name").value("bob"))
                .andExpect(jsonPath("adminGroup.members[0].name").value("o4-name"))
                //.andExpect(jsonPath("name").value("bob"))
                //.andExpect(jsonPath("basis.members[0].name").value("b0-name"))
        ;
    }
    */

    //Hi.
    //Hi.
    //Hi.
    //HiEND!!!.

    @Test
    @WithMockUhUser
    public void rootTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/groupings/v2.1/"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("University of Hawaii Groupings API", result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUhUser
    public void getGrouping() throws Exception {
        final String grouping = "grouping";
        final String username = "user";

        given(groupingAssignmentService.getGrouping(grouping, username))
                .willReturn(grouping());

        mockMvc.perform(get("/api/groupings/v2.0/grouping/grouping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("bob"))
                .andExpect(jsonPath("path").value("test:ing:me:bob"))
                .andExpect(jsonPath("listservOn").value("true"))
                .andExpect(jsonPath("basis.members", hasSize(3)))
                .andExpect(jsonPath("basis.members[0].name").value("b0-name"))
                .andExpect(jsonPath("basis.members[0].uuid").value("b0-uuid"))

                .andExpect(jsonPath("basis.members[0].username").value("b0-username"))
                .andExpect(jsonPath("basis.members[1].name").value("b1-name"))
                .andExpect(jsonPath("basis.members[1].uuid").value("b1-uuid"))
                .andExpect(jsonPath("basis.members[1].username").value("b1-username"))
                .andExpect(jsonPath("basis.members[2].name").value("b2-name"))
                .andExpect(jsonPath("basis.members[2].uuid").value("b2-uuid"))
                .andExpect(jsonPath("basis.members[2].username").value("b2-username"))
                .andExpect(jsonPath("exclude.members", hasSize(1)))
                .andExpect(jsonPath("exclude.members[0].name").value("e0-name"))
                .andExpect(jsonPath("exclude.members[0].name").value("e0-name"))
                .andExpect(jsonPath("exclude.members[0].uuid").value("e0-uuid"))
                .andExpect(jsonPath("include.members", hasSize(2)))
                .andExpect(jsonPath("include.members[1].name").value("i1-name"))
                .andExpect(jsonPath("include.members[1].name").value("i1-name"))
                .andExpect(jsonPath("include.members[1].uuid").value("i1-uuid"))
                .andExpect(jsonPath("owners.members", hasSize(4)))
                .andExpect(jsonPath("owners.members[3].name").value("o3-name"))
                .andExpect(jsonPath("owners.members[3].uuid").value("o3-uuid"))
                .andExpect(jsonPath("owners.members[3].username").value("o3-username"))
                .andExpect(jsonPath("composite.members", hasSize(0)));
    }

    @Test
    @WithMockUhUser(username = "admin")
    public void addAdminTest() throws Exception {
        given(membershipService.addAdmin("admin", "newAdmin"))
                .willReturn(new GroupingsServiceResult("SUCCESS", "add admin"));

        mockMvc.perform(post("/api/groupings/v2.0/newAdmin/addAdmin")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("add admin"));
    }

    @Test
    @WithMockUhUser(username = "admin")
    public void deleteAdminTest() throws Exception {
        given(membershipService.deleteAdmin("admin", "newAdmin"))
                .willReturn(new GroupingsServiceResult("SUCCESS", "delete admin"));

        mockMvc.perform(post("/api/groupings/v2.0/newAdmin/deleteAdmin")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("delete admin"));
    }

    @Test
    @WithMockUhUser
    public void addByUsernameTest() throws Exception {

        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult("SUCCESS", "add grouping member by username"));

        given(membershipService.addGroupingMemberByUsername("user", "grouping", "user"))
                .willReturn(gsrList);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/user/addGroupingMemberByUsername")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("add grouping member by username"));
    }

    @Test
    @WithMockUhUser
    public void addByUuIDTest() throws Exception {

        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult("SUCCESS", "add grouping member by uuid"));

        given(membershipService.addGroupingMemberByUuid("user", "grouping", "user"))
                .willReturn(gsrList);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/user/addGroupingMemberByUuid")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("add grouping member by uuid"));
    }

    @Test
    @WithMockUhUser
    public void getAddMember() throws Exception {
        final String grouping = "grouping";
        final String username = "user";
        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        List<GroupingsServiceResult> gsrList2 = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult("SUCCESS", "add member to include group"));
        gsrList2.add(new GroupingsServiceResult("SUCCESS", "add member to exclude group"));

        given(membershipService.addGroupMemberByUsername(username, grouping + ":include", username))
                .willReturn(gsrList);
        given(membershipService.addGroupMemberByUsername(username, grouping + ":exclude", username))
                .willReturn(gsrList2);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/user/addMemberToIncludeGroup")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("add member to include group"));

        mockMvc.perform(post("/api/groupings/v2.0/grouping/user/addMemberToExcludeGroup")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("add member to exclude group"));
    }

    @Test
    @WithMockUhUser
    public void deleteByUsernameTest() throws Exception {

        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult("SUCCESS", "delete grouping member by username"));

        //new GroupingsServiceResult("SUCCESS", "delete grouping member by username")

        given(membershipService.deleteGroupingMemberByUsername("user", "grouping", "user"))
                .willReturn(gsrList);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/user/deleteGroupingMemberByUsername")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("delete grouping member by username"));
    }

    @Test
    @WithMockUhUser
    public void deleteByUuIDTest() throws Exception {

        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult("SUCCESS", "delete grouping member by uuid"));

        //new GroupingsServiceResult("SUCCESS", "delete grouping member by username")

        given(membershipService.deleteGroupingMemberByUuid("user", "grouping", "user"))
                .willReturn(gsrList);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/user/deleteGroupingMemberByUuid")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("delete grouping member by uuid"));
    }

    @Test
    @WithMockUhUser
    public void getDeleteMember() throws Exception {
        final String grouping = "grouping";
        final String username = "user";
        GroupingsServiceResult gsr = new GroupingsServiceResult("SUCCESS", "delete member from include group");
        GroupingsServiceResult gsr2 = new GroupingsServiceResult("SUCCESS", "delete member from exclude group");

        given(membershipService.deleteGroupMemberByUsername(username, grouping + ":include", username))
                .willReturn(gsr);
        given(membershipService.deleteGroupMemberByUsername(username, grouping + ":exclude", username))
                .willReturn(gsr2);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/user/deleteMemberFromIncludeGroup")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("delete member from include group"));

        mockMvc.perform(post("/api/groupings/v2.0/grouping/user/deleteMemberFromExcludeGroup")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("delete member from exclude group"));
    }

    @Test
    @WithMockUhUser
    public void getAssignOwnership() throws Exception {
        final String grouping = "grouping";
        final String username = "user";
        GroupingsServiceResult gsr;

        gsr = new GroupingsServiceResult("SUCCESS", "give user ownership of grouping");

        given(memberAttributeService.assignOwnership(grouping, username, username))
                .willReturn(gsr);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/user/assignOwnership")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("give user ownership of grouping"));
    }

    @Test
    @WithMockUhUser
    public void getRemoveOwnership() throws Exception {
        final String grouping = "grouping";
        final String username = "user";
        GroupingsServiceResult gsr;

        gsr = new GroupingsServiceResult("SUCCESS", "remove user's ownership privilege for grouping");

        given(memberAttributeService.removeOwnership(grouping, username, username))
                .willReturn(gsr);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/user/removeOwnership")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("remove user's ownership privilege for grouping"));
    }

    @Test
    @WithMockUhUser
    public void getMyGroupings() throws Exception {
        ObjectMapper om = new ObjectMapper();
        final String username = "user";
        List<Grouping> groupings = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            groupings.add(grouping());
            groupings.get(i).setPath("grouping" + i);
        }

        given(groupingAssignmentService.getGroupingAssignment(username))
                .willReturn(myGroupings());

        String mvcResult = mockMvc.perform(get("/api/groupings/v2.0/groupingAssignment"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        GroupingAssignment mg = om.readValue(mvcResult, GroupingAssignment.class);

        Assert.assertTrue(mg.getGroupingsIn().get(0).getName().equals(groupings.get(0).getName()));
        Assert.assertTrue(mg.getGroupingsIn().get(0).getPath().equals(groupings.get(0).getPath()));
        Assert.assertTrue(
                mg.getGroupingsIn().get(0).getOwners().getNames().equals(groupings.get(0).getOwners().getNames()));
        Assert.assertTrue(mg.getGroupingsIn().get(0).getOwners().getUsernames()
                .equals(groupings.get(0).getOwners().getUsernames()));
        Assert.assertTrue(
                mg.getGroupingsIn().get(0).getOwners().getUuids().equals(groupings.get(0).getOwners().getUuids()));
        Assert.assertTrue(mg.getGroupingsOwned().get(0).getName().equals(groupings.get(0).getName()));
        Assert.assertTrue(mg.getGroupingsOwned().get(0).getPath().equals(groupings.get(0).getPath()));
        Assert.assertTrue(
                mg.getGroupingsOwned().get(0).getOwners().getNames().equals(groupings.get(0).getOwners().getNames()));
        Assert.assertTrue(mg.getGroupingsOwned().get(0).getOwners().getUsernames()
                .equals(groupings.get(0).getOwners().getUsernames()));
        Assert.assertTrue(
                mg.getGroupingsOwned().get(0).getOwners().getUuids().equals(groupings.get(0).getOwners().getUuids()));
        Assert.assertTrue(mg.getGroupingsToOptInTo().get(0).getName().equals(groupings.get(0).getName()));
        Assert.assertTrue(mg.getGroupingsToOptInTo().get(0).getPath().equals(groupings.get(0).getPath()));
        Assert.assertTrue(mg.getGroupingsToOptInTo().get(0).getOwners().getNames()
                .equals(groupings.get(0).getOwners().getNames()));
        Assert.assertTrue(mg.getGroupingsToOptInTo().get(0).getOwners().getUsernames()
                .equals(groupings.get(0).getOwners().getUsernames()));
        Assert.assertTrue(mg.getGroupingsToOptInTo().get(0).getOwners().getUuids()
                .equals(groupings.get(0).getOwners().getUuids()));
        Assert.assertTrue(mg.getGroupingsToOptOutOf().get(0).getName().equals(groupings.get(0).getName()));
        Assert.assertTrue(mg.getGroupingsToOptOutOf().get(0).getPath().equals(groupings.get(0).getPath()));
        Assert.assertTrue(mg.getGroupingsToOptOutOf().get(0).getOwners().getNames()
                .equals(groupings.get(0).getOwners().getNames()));
        Assert.assertTrue(mg.getGroupingsToOptOutOf().get(0).getOwners().getUsernames()
                .equals(groupings.get(0).getOwners().getUsernames()));
        Assert.assertTrue(mg.getGroupingsToOptOutOf().get(0).getOwners().getUuids()
                .equals(groupings.get(0).getOwners().getUuids()));
        Assert.assertTrue(mg.getGroupingsOptedInTo().get(0).getName().equals(groupings.get(0).getName()));
        Assert.assertTrue(mg.getGroupingsOptedInTo().get(0).getPath().equals(groupings.get(0).getPath()));
        Assert.assertTrue(mg.getGroupingsOptedInTo().get(0).getOwners().getNames()
                .equals(groupings.get(0).getOwners().getNames()));
        Assert.assertTrue(mg.getGroupingsOptedInTo().get(0).getOwners().getUsernames()
                .equals(groupings.get(0).getOwners().getUsernames()));
        Assert.assertTrue(mg.getGroupingsOptedInTo().get(0).getOwners().getUuids()
                .equals(groupings.get(0).getOwners().getUuids()));
        Assert.assertTrue(mg.getGroupingsOptedOutOf().get(0).getName().equals(groupings.get(0).getName()));
        Assert.assertTrue(mg.getGroupingsOptedOutOf().get(0).getPath().equals(groupings.get(0).getPath()));
        Assert.assertTrue(mg.getGroupingsOptedOutOf().get(0).getOwners().getNames()
                .equals(groupings.get(0).getOwners().getNames()));
        Assert.assertTrue(mg.getGroupingsOptedOutOf().get(0).getOwners().getUsernames()
                .equals(groupings.get(0).getOwners().getUsernames()));
        Assert.assertTrue(mg.getGroupingsOptedOutOf().get(0).getOwners().getUuids()
                .equals(groupings.get(0).getOwners().getUuids()));
    }

    @Test
    @WithMockUhUser
    public void getSetListserv() throws Exception {
        final String grouping = "grouping";
        final String username = "user";
        GroupingsServiceResult gsr = new GroupingsServiceResult("SUCCESS", "listserv has been added to grouping");
        GroupingsServiceResult gsr2 = new GroupingsServiceResult("SUCCESS", "listserv has been removed from grouping");

        given(groupAttributeService.changeListservStatus(grouping, username, true))
                .willReturn(gsr);
        given(groupAttributeService.changeListservStatus(grouping, username, false))
                .willReturn(gsr2);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/true/setListserv")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("listserv has been added to grouping"));

        mockMvc.perform(post("/api/groupings/v2.0/grouping/false/setListserv")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value("SUCCESS"))
                .andExpect(jsonPath("action").value("listserv has been removed from grouping"));
    }

    @Test
    @WithMockUhUser
    public void getSetOptIn() throws Exception {
        final String grouping = "grouping";
        final String username = "user";
        List<GroupingsServiceResult> gsResults = new ArrayList<>();
        List<GroupingsServiceResult> gsResults2 = new ArrayList<>();
        GroupingsServiceResult gsr = new GroupingsServiceResult("SUCCESS", "OptIn has been added to grouping");
        GroupingsServiceResult gsr2 = new GroupingsServiceResult("SUCCESS", "OptIn has been removed from grouping");
        gsResults.add(gsr);
        gsResults2.add(gsr2);

        given(groupAttributeService.changeOptInStatus(grouping, username, true))
                .willReturn(gsResults);
        given(groupAttributeService.changeOptInStatus(grouping, username, false))
                .willReturn(gsResults2);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/true/setOptIn")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("OptIn has been added to grouping"));

        mockMvc.perform(post("/api/groupings/v2.0/grouping/false/setOptIn")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("OptIn has been removed from grouping"));
    }

    @Test
    @WithMockUhUser
    public void getSetOptOut() throws Exception {
        final String grouping = "grouping";
        final String username = "user";
        List<GroupingsServiceResult> gsResults = new ArrayList<>();
        List<GroupingsServiceResult> gsResults2 = new ArrayList<>();
        GroupingsServiceResult gsr = new GroupingsServiceResult("SUCCESS", "OptOut has been added to grouping");
        GroupingsServiceResult gsr2 = new GroupingsServiceResult("SUCCESS", "OptOut has been removed from grouping");
        gsResults.add(gsr);
        gsResults2.add(gsr2);

        given(groupAttributeService.changeOptOutStatus(grouping, username, true))
                .willReturn(gsResults);
        given(groupAttributeService.changeOptOutStatus(grouping, username, false))
                .willReturn(gsResults2);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/true/setOptOut")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("OptOut has been added to grouping"));

        mockMvc.perform(post("/api/groupings/v2.0/grouping/false/setOptOut")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("OptOut has been removed from grouping"));
    }

    @Test
    @WithMockUhUser
    public void getOptIn() throws Exception {
        final String grouping = "grouping";
        final String username = "user";
        List<GroupingsServiceResult> gsr = new ArrayList<>();

        gsr.add(new GroupingsServiceResult("SUCCESS", "delete member from exclude group"));
        gsr.add(new GroupingsServiceResult("SUCCESS", "add member to include group"));
        gsr.add(new GroupingsServiceResult("SUCCESS", "add self-opted attribute to include group"));
        gsr.add(new GroupingsServiceResult("SUCCESS", "remove self-opted attribute to exclude group"));
        gsr.add(new GroupingsServiceResult("SUCCESS", "update last-modified attribute for exclude group"));
        gsr.add(new GroupingsServiceResult("SUCCESS", "update last-modified attribute for include group"));

        given(membershipService.optIn(username, grouping))
                .willReturn(gsr);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/optIn")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("delete member from exclude group"))
                .andExpect(jsonPath("$[1].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[1].action").value("add member to include group"))
                .andExpect(jsonPath("$[2].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[2].action").value("add self-opted attribute to include group"))
                .andExpect(jsonPath("$[3].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[3].action").value("remove self-opted attribute to exclude group"))
                .andExpect(jsonPath("$[4].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[4].action").value("update last-modified attribute for exclude group"))
                .andExpect(jsonPath("$[5].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[5].action").value("update last-modified attribute for include group"));
    }

    @Test
    @WithMockUhUser
    public void getOptOut() throws Exception {
        final String grouping = "grouping";
        final String username = "user";
        List<GroupingsServiceResult> gsr = new ArrayList<>();

        gsr.add(new GroupingsServiceResult("SUCCESS", "delete member from include group"));
        gsr.add(new GroupingsServiceResult("SUCCESS", "add member to exclude group"));
        gsr.add(new GroupingsServiceResult("SUCCESS", "add self-opted attribute to exclude group"));
        gsr.add(new GroupingsServiceResult("SUCCESS", "remove self-opted attribute to include group"));
        gsr.add(new GroupingsServiceResult("SUCCESS", "update last-modified attribute for include group"));
        gsr.add(new GroupingsServiceResult("SUCCESS", "update last-modified attribute for exclude group"));

        given(membershipService.optOut(username, grouping))
                .willReturn(gsr);

        mockMvc.perform(post("/api/groupings/v2.0/grouping/optOut")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[0].action").value("delete member from include group"))
                .andExpect(jsonPath("$[1].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[1].action").value("add member to exclude group"))
                .andExpect(jsonPath("$[2].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[2].action").value("add self-opted attribute to exclude group"))
                .andExpect(jsonPath("$[3].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[3].action").value("remove self-opted attribute to include group"))
                .andExpect(jsonPath("$[4].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[4].action").value("update last-modified attribute for include group"))
                .andExpect(jsonPath("$[5].resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$[5].action").value("update last-modified attribute for exclude group"));
    }


    @Test
    @WithMockUhUser(username = "admin")
    public void adminListsTest() throws Exception {

        String mvcResult = mockMvc.perform(get("/api/groupings/v2.0/adminLists"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

    }

    @Test
    @WithMockUhUser
    public void getAddGrouping() throws Exception {

        mockMvc.perform(post("/api/groupings/v2.0/fakeGroup/fakeBasis/fakeIncldue/fakeExclude/fakeOwners/addGrouping")
                .with(csrf()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUhUser
    public void getDeleteGrouping() throws Exception {
        mockMvc.perform(delete("/api/groupings/v2.0/fakeGroup/deleteGrouping")
                .with(csrf()))
                .andExpect(status().is5xxServerError());
    }
}
