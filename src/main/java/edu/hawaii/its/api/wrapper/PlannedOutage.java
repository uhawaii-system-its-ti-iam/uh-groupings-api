package edu.hawaii.its.api.wrapper;
import edu.hawaii.its.api.util.Dates;

import java.time.LocalDate;
import java.time.Month;

public class PlannedOutage /*extends PlannedOutageResult*/ {
    public static String returnMessage() {
        return "This site is down for maintenance.";
//        return null;
//        LocalDate currDate = LocalDate.now();
//        PlannedOutageResult theResult = new PlannedOutageResult();
//
//        Dates result = Dates.formatDate("2025-01-25T06:00:00Z", /*someformat*/, )
//
//        String yearOfOutageStr = theResult.getFrom().substring(0,3); //grabs the year
//        int yearOfOutageInt = Integer.parseInt(yearOfOutageStr);
//
//        String monthOfOutageStr = theResult.getFrom().substring(5,6); //grabs the month
//        int monthOfOutageInt = Integer.parseInt(monthOfOutageStr);
//
//        String dayOfOutageStr = theResult.getFrom().substring(8,10); //grabs the day
//        int dayOfOutageInt = Integer.parseInt(monthOfOutageStr);
////
////        //valid outage message - if response is a Date
//        if (currDate.after(theResult.getFrom()) && currDate.before(theResult.getTo())) {
//            return theResult.getText();
//        }
//        return null;
////
//
//
//        //valid outage message - if response is a String
//        if (currDate.getMonthValue() == monthOfOutageInt && currDate.getDayOfMonth() >= dayOfOutageInt
//            && currDate.getYear() == yearOfOutageInt) {
//            return theResult.getText();
//        } else {
//            return null;
//        }

    }
}
