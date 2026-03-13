package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.HasMemberResult;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.Subject;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingMemberTest {

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void test() {
        Subject subject = groupingsTestConfiguration.subjectSuccessUidTestData();
        GroupingGroupMember groupingGroupMember = new GroupingGroupMember(subject);
        GroupingMember groupingMember = new GroupingMember(groupingGroupMember, "Include");
        assertNotNull(groupingMember);

        assertEquals(TEST_UIDS.get(0), groupingMember.getUid());
        assertEquals(TEST_UH_UUIDS.get(0), groupingMember.getUhUuid());
        assertEquals(TEST_NAMES.get(0), groupingMember.getName());
        assertEquals(TEST_NAMES.get(0).split(" ")[0], groupingMember.getFirstName());
        assertEquals(TEST_NAMES.get(0).split(" ")[1], groupingMember.getLastName());
        assertEquals("Include", groupingMember.getWhereListed());
    }
	
	@Test
	public void constructorWithHasMemberResult() {
		HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUidTestData();
		HasMemberResult hasMemberResult = hasMembersResults.getResults().get(0);
		GroupingMember groupingMember = new GroupingMember(hasMemberResult, "include");
		
		assertNotNull(groupingMember);
		assertEquals(TEST_UIDS.get(0), groupingMember.getUid());
		assertEquals(TEST_UH_UUIDS.get(0), groupingMember.getUhUuid());
		assertEquals(TEST_NAMES.get(0), groupingMember.getName());
		assertEquals(TEST_NAMES.get(0).split(" ")[0], groupingMember.getFirstName());
		assertEquals(TEST_NAMES.get(0).split(" ")[1], groupingMember.getLastName());
		assertEquals("Include", groupingMember.getWhereListed());
	}
	
	@Test
	public void constructorWithHasMemberResultNotMember() {
		HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsNotMembersUidTestData();
		HasMemberResult hasMemberResult = hasMembersResults.getResults().get(0);
		GroupingMember groupingMember = new GroupingMember(hasMemberResult, "include");
		
		assertNotNull(groupingMember);
		assertEquals(TEST_UIDS.get(0), groupingMember.getUid());
		assertEquals("", groupingMember.getWhereListed());
	}
	
	@Test
	public void constructorWithTwoHasMemberResults() {
		HasMembersResults basisResults = groupingsTestConfiguration.hasMemberResultsIsMembersBasisTestData();
		HasMembersResults includeResults = groupingsTestConfiguration.hasMemberResultsIsMembersUidTestData();
		HasMemberResult basisResult = basisResults.getResults().get(0);
		HasMemberResult includeResult = includeResults.getResults().get(0);
		
		GroupingMember groupingMember = new GroupingMember(basisResult, "basis", includeResult, "include");
		
		assertNotNull(groupingMember);
		assertEquals(TEST_UIDS.get(0), groupingMember.getUid());
		assertEquals(TEST_UH_UUIDS.get(0), groupingMember.getUhUuid());
		assertEquals(TEST_NAMES.get(0), groupingMember.getName());
		assertEquals("Basis & Include", groupingMember.getWhereListed());
	}
	
	@Test
	public void constructorWithTwoHasMemberResultsOnlyBasis() {
		HasMembersResults basisResults = groupingsTestConfiguration.hasMemberResultsIsMembersBasisTestData();
		HasMembersResults notIncludeResults = groupingsTestConfiguration.hasMemberResultsIsNotMembersUidTestData();
		HasMemberResult basisResult = basisResults.getResults().get(0);
		HasMemberResult notIncludeResult = notIncludeResults.getResults().get(0);
		
		GroupingMember groupingMember = new GroupingMember(basisResult, "basis", notIncludeResult, "include");
		
		assertNotNull(groupingMember);
		assertEquals(TEST_UIDS.get(0), groupingMember.getUid());
		assertEquals("Basis", groupingMember.getWhereListed());
	}
	
	@Test
	public void constructorWithTwoHasMemberResultsOnlyInclude() {
		HasMembersResults notBasisResults = groupingsTestConfiguration.hasMemberResultsIsNotMembersBasisTestData();
		HasMembersResults includeResults = groupingsTestConfiguration.hasMemberResultsIsMembersUidTestData();
		HasMemberResult notBasisResult = notBasisResults.getResults().get(0);
		HasMemberResult includeResult = includeResults.getResults().get(0);
		
		GroupingMember groupingMember = new GroupingMember(notBasisResult, "basis", includeResult, "include");
		
		assertNotNull(groupingMember);
		assertEquals(TEST_UIDS.get(0), groupingMember.getUid());
		assertEquals("Include", groupingMember.getWhereListed());
	}
	
	@Test
	public void constructorWithTwoHasMemberResultsNeither() {
		HasMembersResults notBasisResults = groupingsTestConfiguration.hasMemberResultsIsNotMembersBasisTestData();
		HasMembersResults notIncludeResults = groupingsTestConfiguration.hasMemberResultsIsNotMembersUidTestData();
		HasMemberResult notBasisResult = notBasisResults.getResults().get(0);
		HasMemberResult notIncludeResult = notIncludeResults.getResults().get(0);
		
		GroupingMember groupingMember = new GroupingMember(notBasisResult, "basis", notIncludeResult, "include");
		
		assertNotNull(groupingMember);
		assertEquals(TEST_UIDS.get(0), groupingMember.getUid());
		assertEquals("", groupingMember.getWhereListed());
	}
	
	@Test
	public void constructorWithHasMemberResultOnly() {
		HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUidTestData();
		HasMemberResult hasMemberResult = hasMembersResults.getResults().get(0);
		GroupingMember groupingMember = new GroupingMember(hasMemberResult);
		
		assertNotNull(groupingMember);
		assertEquals(TEST_UIDS.get(0), groupingMember.getUid());
		assertEquals(TEST_UH_UUIDS.get(0), groupingMember.getUhUuid());
		assertEquals(TEST_NAMES.get(0), groupingMember.getName());
		assertEquals(TEST_NAMES.get(0).split(" ")[0], groupingMember.getFirstName());
		assertEquals(TEST_NAMES.get(0).split(" ")[1], groupingMember.getLastName());
		assertEquals("", groupingMember.getWhereListed());
	}
	
	@Test
	public void defaultConstructor() {
		GroupingMember groupingMember = new GroupingMember();
        assertNotNull(groupingMember);
        assertNotNull(groupingMember.getName());
        assertNotNull(groupingMember.getUhUuid());
        assertNotNull(groupingMember.getUid());
        assertNotNull(groupingMember.getWhereListed());
		assertEquals("", groupingMember.getName());
		assertEquals("", groupingMember.getUhUuid());
		assertEquals("", groupingMember.getUid());
		assertEquals("", groupingMember.getWhereListed());
		assertEquals("", groupingMember.getFirstName());
		assertEquals("", groupingMember.getLastName());
    }

}
