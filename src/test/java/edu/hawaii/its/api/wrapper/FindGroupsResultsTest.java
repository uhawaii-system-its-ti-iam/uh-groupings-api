package edu.hawaii.its.api.wrapper;

import static edu.hawaii.its.api.util.JsonUtil.asObject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;

public class FindGroupsResultsTest {

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

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
        String json = propertyLocator.find("find.groups.results.description");
        FindGroupsResults results =
                new FindGroupsResults(asObject(json, WsFindGroupsResults.class));
        assertThat(results.getResultCode(), equalTo(SUCCESS));
        assertThat(results.getGroup().getDescription(), equalTo("Test Many Groups In Basis"));
        assertThat(results.getGroup().getResultCode(), equalTo(SUCCESS));
        assertNotNull(results.getGroups());
    }

    @Test
    public void getNullDescription() {
        String json = propertyLocator.find("find.groups.results.null.description");
        FindGroupsResults results =
                new FindGroupsResults(asObject(json, WsFindGroupsResults.class));
        assertThat(results.getResultCode(), equalTo(SUCCESS));
        assertThat(results.getGroup().getDescription(), equalTo(""));
        assertThat(results.getGroup().getResultCode(), equalTo(SUCCESS));
        assertNotNull(results.getGroups());
    }

    @Test
    public void getEmptyDescription() {
        String json = propertyLocator.find("find.groups.results.empty.description");
        WsFindGroupsResults wsFindGroupsResults = asObject(json, WsFindGroupsResults.class);
        FindGroupsResults results = new FindGroupsResults(wsFindGroupsResults);
        assertThat(results.getResultCode(), equalTo(SUCCESS));
        assertThat(results.getGroup().getDescription(), equalTo(""));
        assertThat(results.getGroup().getResultCode(), equalTo(SUCCESS));
        assertNotNull(results.getGroups());
    }

    @Test
    public void onFailure() {
        String json = propertyLocator.find("find.groups.results.failure");
        WsFindGroupsResults wsFindGroupsResults = asObject(json, WsFindGroupsResults.class);
        FindGroupsResults results = new FindGroupsResults(wsFindGroupsResults);
        assertThat(results.getResultCode(), equalTo(FAILURE));
    }
}
