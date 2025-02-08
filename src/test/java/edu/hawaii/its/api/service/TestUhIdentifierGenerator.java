//package edu.hawaii.its.api.service;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import edu.hawaii.its.api.configuration.SpringBootWebApplication;
//import edu.hawaii.its.api.groupings.GroupingMember;
//
//@ActiveProfiles("integrationTest")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@SpringBootTest(classes = { SpringBootWebApplication.class })
//public class TestUhIdentifierGenerator {
//
//    @Autowired
//    UhIdentifierGenerator uhIdentifierGenerator;
//
//    @Test
//    public void getRandomUhIdentifierTest() {
//        GroupingMember member = uhIdentifierGenerator.getRandomMember();
//        assertNotNull(member);
//        assertNotNull(member.getUid());
//        assertNotEquals("", member.getUid());
//        assertNotNull(member.getUhUuid());
//        assertNotEquals("", member.getUhUuid());
//    }
//
//    @Test
//    public void getRandomUhIdentifiersTest() {
//        List<GroupingMember> randomMembers = uhIdentifierGenerator.getRandomMembers(5).getMembers();
//        assertEquals(5, randomMembers.size());
//        for (GroupingMember member : randomMembers) {
//            assertNotNull(member);
//            assertNotNull(member.getUid());
//            assertNotEquals("", member.getUid());
//            assertNotNull(member.getUhUuid());
//            assertNotEquals("", member.getUhUuid());
//        }
//    }
//
//}
