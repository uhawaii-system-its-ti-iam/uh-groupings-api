package edu.hawaii.its.api.wrapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class FindGroupsResultsTest {

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void nullConstruction() {
        FindGroupsResults results = new FindGroupsResults(null);
        assertThat(results.getResultCode(), equalTo(FAILURE));
        assertThat(results, is(notNullValue()));
        assertThat(results.getGroup().getResultCode(), equalTo(FAILURE));
        assertThat(results.getGroup().getDescription(), equalTo(""));
        assertNotNull(results.getGroups());
    }

    @Test
    public void emptyConstruction() {
        FindGroupsResults results = new FindGroupsResults(new WsFindGroupsResults());
        assertThat(results.getResultCode(), equalTo(FAILURE));
        assertThat(results.getGroup().getResultCode(), equalTo(FAILURE));
        assertThat(results.getGroup().getDescription(), equalTo(""));
        assertNotNull(results.getGroups());
    }

    @Test
    public void getDescription() {
        FindGroupsResults results =
                groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertThat(results.getResultCode(), equalTo(SUCCESS));
        assertThat(results.getGroup().getDescription(), equalTo("Test Many Groups In Basis"));
        assertThat(results.getGroup().getResultCode(), equalTo(SUCCESS));
        assertNotNull(results.getGroups());
    }

    @Test
    public void getNullDescription() {
        FindGroupsResults results =
                groupingsTestConfiguration.findGroupsResultsNullDescriptionTestData();
        assertThat(results.getResultCode(), equalTo(SUCCESS));
        assertThat(results.getGroup().getDescription(), equalTo(""));
        assertThat(results.getGroup().getResultCode(), equalTo(SUCCESS));
        assertNotNull(results.getGroups());
    }

    @Test
    public void getEmptyDescription() {
        FindGroupsResults results =
                groupingsTestConfiguration.findGroupsResultsEmptyDescriptionTestData();
        assertThat(results.getResultCode(), equalTo(SUCCESS));
        assertThat(results.getGroup().getDescription(), equalTo(""));
        assertThat(results.getGroup().getResultCode(), equalTo(SUCCESS));
        assertNotNull(results.getGroups());
    }

    @Test
    public void onFailure() {
        FindGroupsResults results =
                groupingsTestConfiguration.findGroupsResultsFailureTestData();
        assertThat(results.getResultCode(), equalTo(FAILURE));
    }
}
