//package edu.hawaii.its.api.wrapper;
//
//import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
//import edu.internet2.middleware.grouperClient.ws.beans.WsPlannedoutageResult;
//import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
//
//import java.util.Date;
//
///**
// * A wrapper for WsPlannedoutageResult.
// */
//public class PlannedoutageResult extends Results {
//
//    private final WsPlannedoutageResult wsPlannedoutageResults[];
//
//    /**
//     * Constructor.
//     * @param wsPlannedoutageResults - the whole response from Grouper
//     */
//    public void GetPlannedoutageResult(WsGetPlannedoutageResults wsPlannedoutageResults) {
//        if (wsPlannedoutageResults == null) {
//            this.wsPlannedoutageResults = new WsPlannedoutageResults(); //or null
//        } else {
//            this.wsPlannedoutageResults = wsPlannedoutageResults;
//        }
//    }
//
//    //@Override
//    public String getResultCode() {
//        return this.wsPlannedoutageResults.getResultMetadata().getResultCode();
//    }
//
//    //not really needed? - the announcement as a whole is just an object to be returned
//    public String getAnnouncement(int objNum) {
//        return this.wsPlannedoutageResults[objNum]; //if you want the whole announcement object listed
////        return getSubject().getAnnouncement();
//    }
//
//    public String getMessage(int objNum) {
//        return this.wsPlannedoutageResults[objNum].message; //if "message" is a property
////        return getSubject().getAnnouncement().getText();
//    }
//
//    public Date getFrom(int objNum) {
//        return this.wsPlannedoutageResults[objNum].from; //if "from" is a property
////        return getSubject().getFrom();
//    }
//
//    public Date getTo(int objNum) {
//        return this.wsPlannedoutageResults[objNum].to; //if "to" is a property
////        return getSubject().getTo();
//    }
//
//    /*
//     * is Subject needed?
//     * @return
//     */
//    public Subject getSubject() {
//        WsPlannedoutageResult wsPlannedoutageResults1 = this.wsPlannedoutageResults.getResults();
//        if (isEmpty(wsPlannedoutageResults1)) {
//            return new Subject();
//        }
//        //from GetGroupsResults.java
////        WsSubject wsSubject = wsGetGroupsResults[0].getWsSubject();
////        return wsSubject != null ? new Subject(wsSubject) : new Subject();
//
//    }
//}

