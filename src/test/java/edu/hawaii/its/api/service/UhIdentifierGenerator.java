package edu.hawaii.its.api.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.Person;

@PropertySource(value = "classpath:application-integrationTest.properties")
@Service("UhIdentifierGenerator")
public class UhIdentifierGenerator {

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    private List<String> testUhNumbers;
    private List<String> testUsernames;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    GrouperApiService grouperApiService;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    public static final Log logger = LogFactory.getLog(UhIdentifierGenerator.class);

    public Person getRandomPerson() {
        // Random page number.
        int rand = getRandomNumberBetween(1,5);
        boolean foundUser = false;
        Person user = new Person();
        String uhUuid = "";
        String uid = "";
        while (!foundUser) {
            Grouping gr = groupingAssignmentService.getPaginatedGrouping(GROUPING, ADMIN, rand, 20, null, true);

            // Random user within the page.
            rand = getRandomNumberBetween(0,gr.getBasis().getMembers().size() - 1);

            if (gr.getBasis().getMembers().size() != 0) {
                user = gr.getBasis().getMembers().get(rand);
                uhUuid = gr.getBasis().getMembers().get(rand).getUhUuid();
                uid = gr.getBasis().getMembers().get(rand).getUsername();
                foundUser = true;
            }
            rand = getRandomNumberBetween(1,5);
            if (uid.equals("") || uhUuid.equals("")) {
                foundUser = false;
            }
        }
        logger.debug(String.format("getRandomUhIdentifier(); name: %s; uhUuid: %s; username: %s;",
                user.getName(), user.getUhUuid(), user.getUsername()));
        return user;
    }

    private static int getRandomNumberBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }

}
