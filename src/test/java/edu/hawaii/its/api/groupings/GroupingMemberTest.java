package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.Subject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingMemberTest {

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> TEST_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_NUMBERS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void test() {
        String json = properties.getProperty("ws.subject.success.uid");
        WsSubject wsSubject = JsonUtil.asObject(json, WsSubject.class);
        Subject subject = new Subject(wsSubject);
        GroupingGroupMember groupingGroupMember = new GroupingGroupMember(subject);
        GroupingMember groupingMember = new GroupingMember(groupingGroupMember, "Include");
        assertNotNull(groupingMember);

        assertEquals(TEST_USERNAMES.get(0), groupingMember.getUid());
        assertEquals(TEST_NUMBERS.get(0), groupingMember.getUhUuid());
        assertEquals(TEST_NAMES.get(0), groupingMember.getName());
        assertEquals("Include", groupingMember.getWhereListed());

        groupingMember = new GroupingMember();
        assertNotNull(groupingMember);
        assertNotNull(groupingMember.getName());
        assertNotNull(groupingMember.getUhUuid());
        assertNotNull(groupingMember.getUid());
        assertNotNull(groupingMember.getWhereListed());
    }

}
