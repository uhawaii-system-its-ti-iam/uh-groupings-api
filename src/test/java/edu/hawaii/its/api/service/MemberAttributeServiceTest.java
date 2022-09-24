package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import edu.hawaii.its.api.util.PropertyLocator;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MemberAttributeServiceTest {

    private PropertyLocator propertyLocator;

    @MockBean
    private GrouperApiService grouperApiService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    final String groupAdminPath = "uh-settings:groupingAdmins";
    final String groupOwnerPath = "uh-settings:groupingOwners";
    final String username = "uuu";
    final String uid = "123";

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void construction() {
        assertNotNull(memberAttributeService);
    }

    @Disabled
    @Test
    public void getMemberAttributesSubjectFound() {
        given(grouperApiService.hasMemberResults(groupAdminPath, username))
                .willReturn(makeWsHasMemberResults("IS_MEMBER"));
        given(grouperApiService.hasMemberResults(groupOwnerPath, username))
                .willReturn(makeWsHasMemberResults("IS_MEMBER"));

        String json = propertyLocator.find("subject.found");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        given(grouperApiService.subjectsResults(any())).willReturn(wsGetSubjectsResults);

        Person person = memberAttributeService.getMemberAttributes(username, uid);
        assertThat(person, is(notNullValue()));
    }

    @Disabled
    @Test
    public void getMemberAttributesSubjectNotFound() {
        given(grouperApiService.hasMemberResults(groupAdminPath, username))
                .willReturn(makeWsHasMemberResults("IS_MEMBER"));
        given(grouperApiService.hasMemberResults(groupOwnerPath, username))
                .willReturn(makeWsHasMemberResults("IS_MEMBER"));

        String json = propertyLocator.find("subject.not.found");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        given(grouperApiService.subjectsResults(any())).willReturn(wsGetSubjectsResults);

        assertThrows(UhMemberNotFoundException.class,
                () -> memberAttributeService.getMemberAttributes(username, uid));
    }

    @Test
    public void getMemberAttributesNotAdminNotOwner() {
        given(grouperApiService.hasMemberResults(groupAdminPath, username))
                .willReturn(makeWsHasMemberResults("IS_MEMBER_FALSE"));
        given(grouperApiService.hasMemberResults(groupOwnerPath, username))
                .willReturn(makeWsHasMemberResults("IS_MEMBER_FALSE"));

        String json = propertyLocator.find("subject.found");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        given(grouperApiService.subjectsResults(any())).willReturn(wsGetSubjectsResults);

        Person person = memberAttributeService.getMemberAttributes(username, uid);
        assertThat(person, is(notNullValue()));
    }

    @Disabled
    @Test
    public void getMemberAttributesAdminButNotOwner() {
        given(grouperApiService.hasMemberResults(groupAdminPath, username))
                .willReturn(makeWsHasMemberResults("NOT_IS_MEMBER"));
        given(grouperApiService.hasMemberResults(groupOwnerPath, username))
                .willReturn(makeWsHasMemberResults("IS_MEMBER"));

        String json = propertyLocator.find("subject.found");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        given(grouperApiService.subjectsResults(any())).willReturn(wsGetSubjectsResults);

        Person person = memberAttributeService.getMemberAttributes(username, uid);
        assertThat(person, is(notNullValue()));
    }

    @Disabled
    @Test
    public void getMemberAttributesOwnerButNotAdmin() {
        given(grouperApiService.hasMemberResults(groupAdminPath, username))
                .willReturn(makeWsHasMemberResults("IS_MEMBER"));
        given(grouperApiService.hasMemberResults(groupOwnerPath, username))
                .willReturn(makeWsHasMemberResults("NOT_IS_MEMBER"));

        String json = propertyLocator.find("subject.found");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        given(grouperApiService.subjectsResults(any())).willReturn(wsGetSubjectsResults);

        Person person = memberAttributeService.getMemberAttributes(username, uid);
        assertThat(person, is(notNullValue()));
    }

    @Test
    public void isUhUuid() {
        assertTrue(memberAttributeService.isUhUuid("111111"));
        assertFalse(memberAttributeService.isUhUuid("111-111"));
        assertFalse(memberAttributeService.isUhUuid("iamtst01"));
        assertFalse(memberAttributeService.isUhUuid(null));
    }

    private WsHasMemberResults makeWsHasMemberResults(final String resultCode) {
        return new WsHasMemberResults() {
            @Override
            public WsHasMemberResult[] getResults() {
                WsHasMemberResult a = new WsHasMemberResult() {
                    @Override
                    public WsResultMeta getResultMetadata() {
                        WsResultMeta b = new WsResultMeta() {
                            @Override
                            public String getResultCode() {
                                return resultCode;
                            }
                        };
                        return b;
                    }
                };
                WsHasMemberResult[] results = { a };
                return results;
            }
        };
    }
}
