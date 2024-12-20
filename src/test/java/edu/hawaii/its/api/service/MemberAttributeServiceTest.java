package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MemberAttributeServiceTest {

    private PropertyLocator propertyLocator;

    @MockitoBean
    private GrouperService grouperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    final String groupAdminPath = "uh-settings:groupingAdmins";
    final String groupOwnerPath = "uh-settings:groupingOwners";
    final String uid = "uuu";

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void construction() {
        assertNotNull(memberAttributeService);
    }

    /**
     * Helper - getMemberAttributeResultsSubjectFound, getMemberAttributeResultsSubjectNotFound, getMemberAttributeResultsNotAdminNotOwner, getMemberAttributeResultsAdminButNotOwner, getMemberAttributeResultsOwnerButNotAdmin
     */
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
