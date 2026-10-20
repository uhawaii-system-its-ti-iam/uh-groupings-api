package edu.hawaii.its.api.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.GrouperException;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersCommand;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.GetMembersCommand;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.MemberFilter;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersCommand;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsCommand;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class }, properties = { "grouping.api.server.type=GROUPER" })
public class GrouperApiServiceTest {

    private static final String CURRENT_USER = "testiwta";
    private static final String GROUP_PATH = "group-path:include";
    private static final String PERSON_SOURCE = "UH core LDAP";

    // With a batch size of 2, these 5 identifiers are sent as the 3 batches below.
    private static final int BATCH_SIZE = 2;
    private static final List<String> UH_IDENTIFIERS =
            List.of("00000001", "00000002", "00000003", "00000004", "00000005");
    private static final List<List<String>> BATCHES =
            List.of(List.of("00000001", "00000002"), List.of("00000003", "00000004"), List.of("00000005"));

    @Autowired
    GrouperService grouperService;

    private ExecutorService exec;

    private GrouperApiService grouperApiService;

    @BeforeEach
    public void setUp() {
        exec = mock(ExecutorService.class);
        grouperApiService = new GrouperApiService(exec, batchExecutor(exec));
    }

    /**
     * Batches of BATCH_SIZE, run in the calling thread, in order, and never sent again.
     */
    private static BatchExecutor batchExecutor(ExecutorService exec) {
        return new BatchExecutor(exec, Runnable::run, BATCH_SIZE, BATCH_SIZE, 4, List.of());
    }

    @Test
    public void isGrouperApiService(){
        assertThat(grouperService, notNullValue());
        assertThat( grouperService instanceof GrouperApiService, equalTo(true));
    }

    @Test
    public void addMembersSendsAListThatFitsInOneBatchAsOneRequest() {
        AddMembersResults addMembersResults = addMembersResults(BATCHES.get(0));
        given(exec.execute(any(AddMembersCommand.class))).willReturn(addMembersResults);

        try (MockedConstruction<AddMembersCommand> commands = mockCommands(AddMembersCommand.class)) {
            assertSame(addMembersResults, grouperApiService.addMembers(CURRENT_USER, GROUP_PATH, BATCHES.get(0)));

            assertEquals(1, commands.constructed().size());
            verify(commands.constructed().get(0)).addUhIdentifiers(BATCHES.get(0), Map.of());
        }
    }

    @Test
    public void addMembersLooksEachMemberUpInItsSubjectSourceAndReportsEachBatch() {
        Map<String, String> sourceIds = Map.of("00000001", PERSON_SOURCE, "00000004", "other-source");
        given(exec.execute(any(AddMembersCommand.class))).willReturn(
                addMembersResults(BATCHES.get(0)), addMembersResults(BATCHES.get(1)), addMembersResults(BATCHES.get(2)));
        List<Integer> batchesDone = new ArrayList<>();

        try (MockedConstruction<AddMembersCommand> commands = mockCommands(AddMembersCommand.class)) {
            grouperApiService.addMembers(CURRENT_USER, GROUP_PATH, UH_IDENTIFIERS, sourceIds, batchesDone::add);

            for (int i = 0; i < BATCHES.size(); i++) {
                verify(commands.constructed().get(i)).addUhIdentifiers(BATCHES.get(i), sourceIds);
            }
            assertEquals(List.of(2, 2, 1), batchesDone);
        }
    }

    @Test
    public void addMembersSendsALongListInBatches() {
        given(exec.execute(any(AddMembersCommand.class))).willReturn(
                addMembersResults(BATCHES.get(0)), addMembersResults(BATCHES.get(1)), addMembersResults(BATCHES.get(2)));

        try (MockedConstruction<AddMembersCommand> commands = mockCommands(AddMembersCommand.class)) {
            AddMembersResults addMembersResults = grouperApiService.addMembers(CURRENT_USER, GROUP_PATH, UH_IDENTIFIERS);

            assertEquals(BATCHES.size(), commands.constructed().size());
            for (int i = 0; i < BATCHES.size(); i++) {
                AddMembersCommand command = commands.constructed().get(i);
                verify(command).owner(CURRENT_USER);
                verify(command).assignGroupPath(GROUP_PATH);
                verify(command).addUhIdentifiers(BATCHES.get(i), Map.of());
            }
            verify(exec, times(BATCHES.size())).execute(any(AddMembersCommand.class));

            assertEquals(GROUP_PATH, addMembersResults.getGroupPath());
            assertEquals("SUCCESS", addMembersResults.getResultCode());
            assertEquals(UH_IDENTIFIERS,
                    addMembersResults.getResults().stream().map(AddMemberResult::getUhUuid).toList());
        }
    }

    @Test
    public void addMembersStopsAtAFailedBatch() {
        given(exec.execute(any(AddMembersCommand.class)))
                .willReturn(addMembersResults(BATCHES.get(0)))
                .willThrow(new GrouperException("Grouper unavailable"));

        try (MockedConstruction<AddMembersCommand> commands = mockCommands(AddMembersCommand.class)) {
            assertThrows(GrouperException.class,
                    () -> grouperApiService.addMembers(CURRENT_USER, GROUP_PATH, UH_IDENTIFIERS));

            // The batch after the failed one is never sent.
            assertEquals(2, commands.constructed().size());
            verify(exec, times(2)).execute(any(AddMembersCommand.class));
        }
    }

    @Test
    public void addMembersSendsNothingForAnEmptyList() {
        assertTrue(grouperApiService.addMembers(CURRENT_USER, GROUP_PATH, List.of()).getResults().isEmpty());
        verifyNoInteractions(exec);
    }

    @Test
    public void removeMembersSendsALongListInBatches() {
        given(exec.execute(any(RemoveMembersCommand.class))).willReturn(
                removeMembersResults(BATCHES.get(0)), removeMembersResults(BATCHES.get(1)),
                removeMembersResults(BATCHES.get(2)));

        try (MockedConstruction<RemoveMembersCommand> commands = mockCommands(RemoveMembersCommand.class)) {
            RemoveMembersResults removeMembersResults =
                    grouperApiService.removeMembers(CURRENT_USER, GROUP_PATH, UH_IDENTIFIERS);

            assertEquals(BATCHES.size(), commands.constructed().size());
            for (int i = 0; i < BATCHES.size(); i++) {
                RemoveMembersCommand command = commands.constructed().get(i);
                verify(command).owner(CURRENT_USER);
                verify(command).assignGroupPath(GROUP_PATH);
                verify(command).addUhIdentifiers(BATCHES.get(i), Map.of());
            }
            verify(exec, times(BATCHES.size())).execute(any(RemoveMembersCommand.class));

            assertEquals(GROUP_PATH, removeMembersResults.getGroupPath());
            assertEquals("SUCCESS", removeMembersResults.getResultCode());
            assertEquals(UH_IDENTIFIERS,
                    removeMembersResults.getResults().stream().map(RemoveMemberResult::getUhUuid).toList());
        }
    }

    @Test
    public void removeMembersSendsNothingForAnEmptyList() {
        assertTrue(grouperApiService.removeMembers(CURRENT_USER, GROUP_PATH, List.of()).getResults().isEmpty());
        verifyNoInteractions(exec);
    }

    @Test
    public void removeMembersLooksEachMemberUpInItsSubjectSourceAndReportsEachBatch() {
        Map<String, String> sourceIds = Map.of("00000002", PERSON_SOURCE);
        given(exec.execute(any(RemoveMembersCommand.class))).willReturn(
                removeMembersResults(BATCHES.get(0)), removeMembersResults(BATCHES.get(1)),
                removeMembersResults(BATCHES.get(2)));
        List<Integer> batchesDone = new ArrayList<>();

        try (MockedConstruction<RemoveMembersCommand> commands = mockCommands(RemoveMembersCommand.class)) {
            grouperApiService.removeMembers(CURRENT_USER, GROUP_PATH, UH_IDENTIFIERS, sourceIds, batchesDone::add);

            for (int i = 0; i < BATCHES.size(); i++) {
                verify(commands.constructed().get(i)).addUhIdentifiers(BATCHES.get(i), sourceIds);
            }
            assertEquals(List.of(2, 2, 1), batchesDone);
        }
    }

    @Test
    public void getSubjectsSendsAListThatFitsInOneBatchAsOneRequest() {
        SubjectsResults subjectsResults = subjectsResults(BATCHES.get(0));
        given(exec.execute(any(SubjectsCommand.class))).willReturn(subjectsResults);

        try (MockedConstruction<SubjectsCommand> commands = mockCommands(SubjectsCommand.class)) {
            assertSame(subjectsResults, grouperApiService.getSubjects(BATCHES.get(0)));

            assertEquals(1, commands.constructed().size());
            // Without a subject source, Grouper looks in every source.
            verify(commands.constructed().get(0)).addSubjects(BATCHES.get(0), null);
        }
    }

    @Test
    public void getSubjectsLooksUpInTheGivenSubjectSourceAndReportsEachBatch() {
        given(exec.execute(any(SubjectsCommand.class))).willReturn(
                subjectsResults(BATCHES.get(0)), subjectsResults(BATCHES.get(1)), subjectsResults(BATCHES.get(2)));
        List<Integer> batchesDone = new ArrayList<>();

        try (MockedConstruction<SubjectsCommand> commands = mockCommands(SubjectsCommand.class)) {
            grouperApiService.getSubjects(UH_IDENTIFIERS, PERSON_SOURCE, batchesDone::add);

            for (int i = 0; i < BATCHES.size(); i++) {
                verify(commands.constructed().get(i)).addSubjects(BATCHES.get(i), PERSON_SOURCE);
            }
            assertEquals(List.of(2, 2, 1), batchesDone);
        }
    }

    @Test
    public void getSubjectsLooksUpALongListInBatches() {
        given(exec.execute(any(SubjectsCommand.class))).willReturn(
                subjectsResults(BATCHES.get(0)), subjectsResults(BATCHES.get(1)), subjectsResults(BATCHES.get(2)));

        try (MockedConstruction<SubjectsCommand> commands = mockCommands(SubjectsCommand.class)) {
            SubjectsResults subjectsResults = grouperApiService.getSubjects(UH_IDENTIFIERS);

            assertEquals(BATCHES.size(), commands.constructed().size());
            for (int i = 0; i < BATCHES.size(); i++) {
                verify(commands.constructed().get(i)).addSubjects(BATCHES.get(i), null);
            }
            verify(exec, times(BATCHES.size())).execute(any(SubjectsCommand.class));

            assertTrue(subjectsResults.isSuccessful());
            assertEquals(UH_IDENTIFIERS,
                    subjectsResults.getUnfilteredSubjects().stream().map(Subject::getUhUuid).toList());
        }
    }

    @Test
    public void getSubjectsKeepsGoingPastABatchInWhichNothingResolves() {
        // Grouper answers a batch of only unknown identifiers with one collapsed SUBJECT_NOT_FOUND entry. The real
        // ExecutorService treats that as an unsuccessful result but, without retry, returns it instead of throwing.
        List<SubjectsResults> answers =
                List.of(subjectsResults(BATCHES.get(0)), notFoundResults(), subjectsResults(BATCHES.get(2)));
        ExecutorService realExec = new ExecutorService();
        GrouperApiService service = new GrouperApiService(realExec, batchExecutor(realExec));

        try (MockedConstruction<SubjectsCommand> commands = mockConstruction(SubjectsCommand.class,
                withSettings().defaultAnswer(RETURNS_SELF),
                (command, context) -> given(command.execute()).willReturn(answers.get(context.getCount() - 1)))) {
            SubjectsResults subjectsResults = service.getSubjects(UH_IDENTIFIERS);

            assertEquals(BATCHES.size(), commands.constructed().size());
            assertTrue(subjectsResults.isSuccessful());
            assertEquals(List.of("00000001", "00000002", "", "00000005"),
                    subjectsResults.getUnfilteredSubjects().stream().map(Subject::getUhUuid).toList());
        }
    }

    @Test
    public void getImmediateMembersCanLeaveOutSubjectDetails() {
        WsGetMembersResult wsGetMembersResult = new WsGetMembersResult();
        wsGetMembersResult.setWsGroup(wsGroup());
        wsGetMembersResult.setWsSubjects(UH_IDENTIFIERS.stream().map(this::wsSubject).toArray(WsSubject[]::new));
        WsGetMembersResults wsGetMembersResults = new WsGetMembersResults();
        wsGetMembersResults.setResults(new WsGetMembersResult[] { wsGetMembersResult });
        given(exec.execute(any(GetMembersCommand.class))).willReturn(new GetMembersResults(wsGetMembersResults));

        try (MockedConstruction<GetMembersCommand> commands = mockCommands(GetMembersCommand.class)) {
            GetMembersResult getMembersResult = grouperApiService.getImmediateMembers(CURRENT_USER, GROUP_PATH, false);

            assertEquals(1, commands.constructed().size());
            GetMembersCommand command = commands.constructed().get(0);
            verify(command).owner(CURRENT_USER);
            verify(command).addGroupPath(GROUP_PATH);
            verify(command).assignMemberFilter(MemberFilter.IMMEDIATE);
            verify(command).includeSubjectDetail(false);
            assertEquals(UH_IDENTIFIERS, getMembersResult.getSubjects().stream().map(Subject::getUhUuid).toList());
        }
    }

    /**
     * While open, every command of commandClass that is built is a mock whose builder methods return the command
     * itself, so a test can verify what each command was built with.
     */
    private static <T> MockedConstruction<T> mockCommands(Class<T> commandClass) {
        return mockConstruction(commandClass, withSettings().defaultAnswer(RETURNS_SELF));
    }

    private AddMembersResults addMembersResults(List<String> uhUuids) {
        WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();
        wsAddMemberResults.setWsGroupAssigned(wsGroup());
        wsAddMemberResults.setResultMetadata(success());
        wsAddMemberResults.setResults(uhUuids.stream().map(uhUuid -> {
            WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
            wsAddMemberResult.setWsSubject(wsSubject(uhUuid));
            wsAddMemberResult.setResultMetadata(success());
            return wsAddMemberResult;
        }).toArray(WsAddMemberResult[]::new));
        return new AddMembersResults(wsAddMemberResults);
    }

    private RemoveMembersResults removeMembersResults(List<String> uhUuids) {
        WsDeleteMemberResults wsDeleteMemberResults = new WsDeleteMemberResults();
        wsDeleteMemberResults.setWsGroup(wsGroup());
        wsDeleteMemberResults.setResultMetadata(success());
        wsDeleteMemberResults.setResults(uhUuids.stream().map(uhUuid -> {
            WsDeleteMemberResult wsDeleteMemberResult = new WsDeleteMemberResult();
            wsDeleteMemberResult.setWsSubject(wsSubject(uhUuid));
            wsDeleteMemberResult.setResultMetadata(success());
            return wsDeleteMemberResult;
        }).toArray(WsDeleteMemberResult[]::new));
        return new RemoveMembersResults(wsDeleteMemberResults);
    }

    private SubjectsResults subjectsResults(List<String> uhUuids) {
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(success());
        wsGetSubjectsResults.setWsSubjects(uhUuids.stream().map(this::wsSubject).toArray(WsSubject[]::new));
        return new SubjectsResults(wsGetSubjectsResults);
    }

    private SubjectsResults notFoundResults() {
        WsSubject notFound = new WsSubject();
        notFound.setResultCode("SUBJECT_NOT_FOUND");
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(success());
        wsGetSubjectsResults.setWsSubjects(new WsSubject[] { notFound });
        return new SubjectsResults(wsGetSubjectsResults);
    }

    private WsGroup wsGroup() {
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName(GROUP_PATH);
        return wsGroup;
    }

    private WsSubject wsSubject(String uhUuid) {
        WsSubject wsSubject = new WsSubject();
        wsSubject.setResultCode("SUCCESS");
        wsSubject.setId(uhUuid);
        return wsSubject;
    }

    private WsResultMeta success() {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS");
        return resultMetadata;
    }
}
