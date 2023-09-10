package edu.hawaii.its.api.wrapper;
//import netscape.javascript.JSObject;
//import jdk.vm.ci.meta.Local;
import edu.hawaii.its.api.groupings.GroupingSyncDestination;
import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.util.Dates;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.api.GcAssignAttributes;
import edu.internet2.middleware.grouperClient.api.GcGetAttributeAssignments;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDef;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefAssignActionResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlannedOutage {
//    protected final GcGetAttributeAssignments test;

    protected WsAttributeAssign assignThis;
    protected WsAttributeDefAssignActionResults theResults;
//    protected final WsAttributeDef attrDef;

    protected final WsAttributeAssign attrDef;
//    protected final WsAttributeDefName attrName;
    public PlannedOutage() {
//        this.test = new GcGetAttributeAssignments();
//        this.test.assignAttributeAssignType("group");


        this.attrDef = new WsAttributeAssign();
        this.assignThis = new GroupAttribute(attrDef);


//        this.theResults = new WsAttributeDefAssignActionResults();
//        this.attrDef = new WsAttributeDef();
//        this.attrName = new WsAttributeDefName();
////        this.attrDef.setAssignToAttributeDef("uh-settings:attributes:for-applications:uhgroupings:propertyString");
//        this.attrDef.setAssignToAttributeDef("uh-settings:attributes:for-applications:uhgroupings:announcements");
//        this.theResults.setWsAttributeDef(this.attrDef);
//        System.out.println("the description of the attr def: " + this.theResults.getResponseMetadata());
    }

//    ublic UpdateTimestampCommand(String groupPath) {
//        Objects.requireNonNull(groupPath, "groupPath cannot be null");
//        this.gcAssignAttributes = new GcAssignAttributes()
//                .assignAttributeAssignType(ASSIGN_TYPE_GROUP)
//                .assignAttributeAssignOperation(OPERATION_ASSIGN_ATTRIBUTE)
//                .addOwnerGroupName(groupPath)
//                .addAttributeDefNameName(YYYYMMDDTHHMM)
//                .assignAttributeAssignValueOperation(OPERATION_REPLACE_VALUES)
//                .addValue(new UpdateTimestampCommand.DateTimeAttributeValue(Dates.formatDate(
//                        Dates.truncateDatePlus60Seconds(LocalDateTime.now()),
//                        DATE_FORMAT)).getWsAttributeAssignValue());
//    }
//
//    private void setSyncDestination(List<AttributesResult> attributesResults) {
//        this.syncDestinations = new ArrayList<>();
//        for (AttributesResult attributesResult : attributesResults) {
//            GroupingSyncDestination groupingSyncDestination =
//                    JsonUtil.asObject(attributesResult.getDescription(), GroupingSyncDestination.class);
//            groupingSyncDestination.setName(attributesResult.getName());
//            groupingSyncDestination.setDescription(groupingSyncDestination.getDescription()
//                    .replaceFirst("\\$\\{srhfgs}", this.groupingExtension));
//            groupingSyncDestination.setSynced(this.groupAttributes.stream()
//                    .anyMatch(groupAttribute -> groupAttribute.getAttributeName().equals(attributesResult.getName())));
//            this.syncDestinations.add(groupingSyncDestination);
//        }
//        this.syncDestinations.sort(Comparator.comparing(GroupingSyncDestination::getDescription));
//    }

    public String returnMessage(/*WsFindAttributeDefNamesResults attr*/) {
        System.out.println("fksjdlkdsjfk: " + this.theResults.getWsAttributeDef());
        return null;
    }
//        FindAttributesResults test = new FindAttributesResults(attr);
//
//        LocalDateTime currDate = LocalDateTime.now();
//        //the whole response from Grouper: a list of objects
////        PlannedOutageResult[] theResults = new PlannedOutageResult();
////        PlannedOutageResult theResults = new PlannedOutageResult().GetPlannedOutageResult("path/to/Grouper");
//        List<Map<String, String>> messages = Arrays.asList(
//                new HashMap<String, String>() {{
//                    put("message", "text to display during date range");
//                    put("from", "20230901T000000");
//                    put("to", "20230903T000000");
//                }},
//                new HashMap<String, String>() {{
//                    put("message", "text to display during date range given by from and to below");
//                    put("from", "20230905T000000");
//                    put("to", "20230908T000000");
//                }}
//        );
//
//        for (int i = 0; i < messages.size(); i++) {
//            //convert dates here
//            LocalDateTime dateTo = Dates.toLocalDateTime(messages.get(i).get("to"), Dates.DATE_FORMAT_PLANNEDOUTAGE);
//            LocalDateTime dateFrom = Dates.toLocalDateTime( messages.get(i).get("from"), Dates.DATE_FORMAT_PLANNEDOUTAGE);
//
//            if (dateFrom.isBefore(currDate) && dateTo.isAfter(currDate)) {
//                return messages.get(i).get("message");
//            }
//        }
//        return null;
//    }
}
