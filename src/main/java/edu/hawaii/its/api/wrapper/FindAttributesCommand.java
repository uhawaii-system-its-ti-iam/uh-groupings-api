package edu.hawaii.its.api.wrapper;

import org.mockito.internal.matchers.Find;

import edu.internet2.middleware.grouperClient.api.GcFindAttributeDefNames;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;

/**
 * A wrapper for FcFindAttributeDefNames. When an attribute definition name is passed, FindAttributesCommand on execute
 * returns results containing all Grouping attributes containing that attribute definition name, EX: passing
 * "uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes" will data pertaining to all grouping sync
 * destinations.
 */
public class FindAttributesCommand extends GrouperCommand implements Command<FindAttributesResults> {
    private final GcFindAttributeDefNames gcFindAttributeDefNames;

    public FindAttributesCommand() {
        this.gcFindAttributeDefNames = new GcFindAttributeDefNames();
    }

    @Override public FindAttributesResults execute() {
        WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = this.gcFindAttributeDefNames.execute();
        return new FindAttributesResults(wsFindAttributeDefNamesResults);
    }

    public FindAttributesCommand assignSearchScope(String scope) {
        this.gcFindAttributeDefNames.assignScope(scope);
        return this;
    }

    public FindAttributesCommand addAttribute(String attribute) {
        this.gcFindAttributeDefNames.addAttributeDefNameName(attribute);
        return this;
    }

    public FindAttributesCommand assignAttributeName(String attributeName) {
        this.gcFindAttributeDefNames.assignNameOfAttributeDef(attributeName);
        return this;
    }

    public FindAttributesCommand owner(String uhIdentifier) {
        this.gcFindAttributeDefNames.assignActAsSubject(subjectLookup(uhIdentifier));
        System.out.println("returning from owner:" + this);
        return this;
    }
}
