package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import java.util.ArrayList;
import java.util.List;

public class MemberAttributeResults implements MemberResults<MemberResult> {

    private String resultCode;
    private List<String> invalid;
    private List<MemberResult> results;

    public MemberAttributeResults() {
        setResultCode("FAILURE");
        setResults(new ArrayList<>());
        setInvalid(new ArrayList<>());
    }

    public MemberAttributeResults(List<String> invalid) {
        setResultCode("FAILURE");
        setResults(new ArrayList<>());
        setInvalid(invalid);
    }

    public MemberAttributeResults(SubjectsResults subjectsResults) {
        setResultCode(subjectsResults.getResultCode());
        setResults(subjectsResults.getSubjects());
        setInvalid(new ArrayList<>());
    }

    /**
     * Builds a result directly from subjects already resolved by an identifier validation (e.g.
     * SubjectService.validateUhIdentifiers), instead of a fresh SubjectsResults - so a caller that already
     * validated the identifiers doesn't have to look them up in Grouper a second time just to get their
     * attributes. Takes a plain List rather than a constructor overload: List&lt;Subject&gt; and List&lt;String&gt;
     * (see the invalid-only constructor) erase to the same signature.
     */
    public static MemberAttributeResults forValidSubjects(List<Subject> subjects) {
        MemberAttributeResults results = new MemberAttributeResults();
        results.setResults(subjects);
        // Matches SubjectsResults.getResultCode(): SUCCESS if any subject would actually appear in getResults()
        // below, not merely if Grouper's result code for it was SUCCESS (an orphan's is, but it has no display
        // attributes and setResults() leaves it out, same as the pre-single-pass caller of this method saw).
        results.setResultCode(results.getResults().isEmpty() ? "FAILURE" : "SUCCESS");
        return results;
    }

    public String getResultCode() {
        return resultCode;
    }

    public List<String> getInvalid() {
        return invalid;
    }

    @Override
    public List<MemberResult> getResults() {
        return results;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    private void setInvalid(List <String> invalid) {
        this.invalid = invalid;
    }

    private void setResults(List<Subject> subjects) {
        this.results = new ArrayList<>();
        for (Subject subject : subjects) {
            // A successful-but-attribute-less ("orphan") subject is a valid Grouper member (validateUhIdentifiers
            // treats it as such) but has nothing to display here, so it's left out - matching what
            // SubjectsResults.getSubjects() already did for the pre-single-pass caller of this method.
            if (subject.getResultCode().equals("SUCCESS") && subject.hasUHAttributes()) {
                this.results.add(new GroupingGroupMember(subject));
            }
        }
    }

}
