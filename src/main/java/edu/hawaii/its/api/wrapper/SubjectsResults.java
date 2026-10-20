package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import edu.hawaii.its.api.type.GroupType;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A wrapper for WsGetSubjectsResults, which is returned from grouper when GcGetSubjects.execute(wrapped by
 * SubjectsCommand) is called. WsGetSubjectsResults contains a list of WsSubject(wrapped by Subject), for each UH
 * identifier queried a WsSubject is added to the list of WsSubject.
 */
public class SubjectsResults extends Results {
    private WsGetSubjectsResults wsGetSubjectsResults;
    private List<Subject> subjects;

    public SubjectsResults(WsGetSubjectsResults wsGetSubjectsResults) {
        if (wsGetSubjectsResults == null) {
            this.wsGetSubjectsResults = new WsGetSubjectsResults();
        } else {
            this.wsGetSubjectsResults = wsGetSubjectsResults;
        }
    }

    public SubjectsResults() {
        this.wsGetSubjectsResults = new WsGetSubjectsResults();
    }

    /**
     * Merge the results of a lookup that was sent to Grouper in batches into one result, in batch order, as if the
     * lookup had been sent in a single request. Each batch collapses its own unresolved lookups into one
     * SUBJECT_NOT_FOUND entry, so the merged result can hold one such entry per batch.
     */
    public static SubjectsResults merge(List<SubjectsResults> batchResults) {
        if (batchResults.isEmpty()) {
            return new SubjectsResults();
        }
        if (batchResults.size() == 1) {
            return batchResults.get(0);
        }
        List<WsGetSubjectsResults> batches =
                batchResults.stream().map(results -> results.wsGetSubjectsResults).toList();
        WsGetSubjectsResults merged = new WsGetSubjectsResults();
        merged.setWsSubjects(batches.stream()
                .map(WsGetSubjectsResults::getWsSubjects)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .toArray(WsSubject[]::new));
        merged.setWsGroup(batches.get(0).getWsGroup());
        merged.setSubjectAttributeNames(batches.get(0).getSubjectAttributeNames());
        merged.setResultMetadata(
                mergedResultMetadata(batches.stream().map(WsGetSubjectsResults::getResultMetadata).toList()));
        return new SubjectsResults(merged);
    }

    public Group getGroup() {
        WsGroup wsGroup = wsGetSubjectsResults.getWsGroup();
        if (wsGroup == null) {
            return new Group();
        }
        return new Group(wsGroup);
    }

    public List<Subject> getSubjects() {
        if (subjects == null) {
            subjects = buildSubjects();
        }
        return new ArrayList<>(subjects);
    }

    private List<Subject> buildSubjects() {
        List<Subject> subjects = new ArrayList<>();
        WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
        if (isEmpty(wsSubjects)) {
            return subjects;
        }
        String groupPath = getGroup().getGroupPath();
        for (WsSubject wsSubject : wsSubjects) {
            if (groupPath.endsWith(GroupType.BASIS.value()) && wsSubject.getSourceId() != null
                    && wsSubject.getSourceId().equals("g:gsa")) {
                continue;
            }
            Subject subject = new Subject(wsSubject);
            if (subject.getResultCode().equals("SUCCESS") && !subject.hasUHAttributes()) {
                continue;
            }
            subjects.add(subject);
        }
        return subjects;
    }

    /**
     * Returns every Subject Grouper answered with, without the "successful but no UH attributes" filtering that
     * getSubjects() applies. Needed when a caller must know which identifiers resolved (e.g. bulk identifier
     * validation), since that filtering would otherwise drop resolved subjects that have no LDAP data.
     * <p>
     * Grouper does not answer with one entry per lookup: lookups that resolve to nothing are collapsed into a
     * single SUBJECT_NOT_FOUND entry, so results can't be correlated with the lookups by position or count.
     */
    public List<Subject> getUnfilteredSubjects() {
        List<Subject> subjects = new ArrayList<>();
        WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
        if (isEmpty(wsSubjects)) {
            return subjects;
        }
        for (WsSubject wsSubject : wsSubjects) {
            subjects.add(new Subject(wsSubject));
        }
        return subjects;
    }

    @Override
    public String getResultCode() {
        String success = "SUCCESS";
        String failure = "FAILURE";
        for (Subject subject : getSubjects()) {
            if (subject.getResultCode().equals(success)) {
                return success;
            }
        }
        return failure;
    }

    public boolean isSuccessful() {
        return getRawResultCode().startsWith("SUCCESS");
    }

    public String getRawResultCode() {
        WsResultMeta resultMetadata = wsGetSubjectsResults.getResultMetadata();
        if (resultMetadata == null) {
            return "";
        }
        String resultCode = resultMetadata.getResultCode();
        return resultCode != null ? resultCode : "";
    }

    @JsonIgnore
    public WsGetSubjectsResults getWsGetSubjectsResults() {
        return this.wsGetSubjectsResults;
    }
}
