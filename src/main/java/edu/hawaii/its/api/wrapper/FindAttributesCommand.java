package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcFindAttributeDefNames;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;

/**
 * A wrapper for FcFindAttributeDefNames. When an attribute definition name is passed, FindAttributesCommand on execute
 * returns results containing all Grouping attributes containing that attribute definition name, EX: passing
 * "uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes" will data pertaining to all grouping sync
 * destinations.
 */
public class FindAttributesCommand extends GrouperCommand<FindAttributesCommand> implements Command<FindAttributesResults> {
    private final GcFindAttributeDefNames gcFindAttributeDefNames;

    public FindAttributesCommand() {
        this.gcFindAttributeDefNames = new GcFindAttributeDefNames();
        this.gcFindAttributeDefNames.assignContentType("text/x-json"); // Remove after upgrading to Grouper 4
    }

    @Override
    public FindAttributesResults execute() {
        WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = this.gcFindAttributeDefNames.execute();
        return new FindAttributesResults(wsFindAttributeDefNamesResults);
    }

    @Override
    protected FindAttributesCommand self() {
        return this;
    }

    public FindAttributesCommand assignSearchScope(String scope) {
        this.gcFindAttributeDefNames.assignScope(scope);
        return this;
    }

    public FindAttributesCommand assignAttributeName(String attributeName) {
        this.gcFindAttributeDefNames.assignNameOfAttributeDef(attributeName);
        return this;
    }

    public FindAttributesCommand owner(String uhIdentifier) {
        this.gcFindAttributeDefNames.assignActAsSubject(subjectLookup(uhIdentifier));
        return this;
    }
}
