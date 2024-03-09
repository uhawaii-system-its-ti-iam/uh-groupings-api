package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = { SpringBootWebApplication.class }, properties = { "grouping.api.server.type=GROUPER" })
public class GrouperApiServiceTest {

    @Autowired
    GrouperService grouperService;

    @Test
    public void isGrouperApiService(){
        assertThat(grouperService, notNullValue());
        assertThat( grouperService instanceof GrouperApiService, equalTo(true));
    }
}
