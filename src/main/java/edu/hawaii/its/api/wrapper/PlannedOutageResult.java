//package edu.hawaii.its.api.wrapper;
//
//import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
//import edu.internet2.middleware.grouperClient.ws.beans.WsPlannedOutageResult;
//import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
//
//import java.util.Date;
//
///**
// * A wrapper for WsPlannedOutageResult.
// */
//public class PlannedOutageResult extends Results {
//
//    private final WsPlannedOutageResult wsPlannedOutageResults;
//
//    /**
//     * Constructor.
//     * @param wsPlannedOutageResults - the whole response from Grouper
//     */
//    public void GetPlannedOutageResult(WsGetPlannedOutageResults wsPlannedOutageResults) {
//        if (wsPlannedOutageResults == null) {
//            this.wsPlannedOutageResults = new WsPlannedOutageResults(); //or null
//        } else {
//            this.wsPlannedOutageResults = wsPlannedOutageResults;
//        }
//    }
//
//    //@Override
//    public String getResultCode() {
//        return this.wsPlannedOutageResults.getResultMetadata().getResultCode();
//    }
//
//    //not really needed? - the announcement as a whole is just an object to be returned
//    public String getAnnouncement(int objNum) {
//        return this.wsPlannedOutageResults[objNum]; //if you want the whole announcement object listed
////        return getSubject().getAnnouncement();
//    }
//
//    public String getMessage(int objNum) {
//        return this.wsPlannedOutageResults[objNum].message; //if "message" is a property
////        return getSubject().getAnnouncement().getText();
//    }
//
//    public Date getFrom(int objNum) {
//        return this.wsPlannedOutageResults[objNum].from; //if "from" is a property
////        return getSubject().getFrom();
//    }
//
//    public Date getTo(int objNum) {
//        return this.wsPlannedOutageResults[objNum].to; //if "to" is a property
////        return getSubject().getTo();
//    }
//
//    /*
//     * is Subject needed?
//     * @return
//     */
//    public Subject getSubject() {
//        WsPlannedOutageResult wsPlannedOutageResults1 = this.wsPlannedOutageResults.getResults();
//        if (isEmpty(wsPlannedOutageResults1)) {
//            return new Subject();
//        }
//        //from GetGroupsResults.java
////        WsSubject wsSubject = wsGetGroupsResults[0].getWsSubject();
////        return wsSubject != null ? new Subject(wsSubject) : new Subject();
//
//    }
//}

