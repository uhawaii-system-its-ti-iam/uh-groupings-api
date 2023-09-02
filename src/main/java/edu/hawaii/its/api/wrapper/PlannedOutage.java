package edu.hawaii.its.api.wrapper;
//import netscape.javascript.JSObject;
//import jdk.vm.ci.meta.Local;
import edu.hawaii.its.api.util.Dates;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlannedOutage /*extends PlannedOutageResult*/ {
    public static String returnMessage() {
//        return "This site is down for maintenance.";
//        return null;
//        LocalDate currDate = LocalDate.now();
        //the whole response from Grouper: a list of objects
//        PlannedOutageResult[] theResults = new PlannedOutageResult();
//        PlannedOutageResult theResults = new PlannedOutageResult().GetPlannedOutageResult("path/to/Grouper");

//        Dates result = Dates.formatDate("20250125T060000", "some format?");  //could use substring approach
//
        List<Map<String, String>> messages = Arrays.asList(
                new HashMap<String, String>() {{
                    put("message", "text to display during date range given by from and to below");
                    put("from", "20230607T000000");
                    put("to", "20230615T000000");
                }},
                new HashMap<String, String>() {{
                    put("message", "text to display during date range given by from and to below");
                    put("from", "20230701T000000");
                    put("to", "20230702T000000");
                }}
        );

       // for (int i = 0; i < messages.size(); i++) {
            System.out.println(messages.get(0).get("from")); //JSON will have
            //convert dates here


            LocalDateTime resultDateTo = Dates.toLocalDateTime(messages.get(0).get("to"), Dates.DATE_FORMAT_PLANNEDOUTAGE);
            LocalDateTime test = Dates.newLocalDateTime(2023, Month.JANUARY, 1);

//            String tester = Dates.formatDate(test, Dates.DATE_FORMAT_PLANNEDOUTAGE);
//            String str = Dates.formatDate("20230607T000000", "yyyyMMddTHHmmss");


            System.out.println("this is: " + resultDateTo);
            //site accessed during a date that falls into the outage message time range
//            if (resultDateFrom <= currDate && resultDateTo >= currDate) {
//                return theResults[i].message; //wouldn't need functions in the Result.java file if can access this way
//            }
        //}




        return "aye";



//        when response is an array of objects
//        for (int i = 0; i < theResults.get(); i++) {
//            //convert dates here
//            Dates resultDateTo = Dates.formatDate(theResults.getTo(i));
//            Dates resultDateFrom = Dates.formatDate(theResults[i].from);
//
//            //site accessed during a date that falls into the outage message time range
//            if (resultDateFrom <= currDate && resultDateTo >= currDate) {
//                return theResults[i].message; //wouldn't need functions in the Result.java file if can access this way
//            }
//        }
//        return null;




//        String yearOfOutageStr = theResults.getFrom().substring(0,3); //grabs the year
//        int yearOfOutageInt = Integer.parseInt(yearOfOutageStr);
//
//        String monthOfOutageStr = theResults.getFrom().substring(5,6); //grabs the month
//        int monthOfOutageInt = Integer.parseInt(monthOfOutageStr);
//
//        String dayOfOutageStr = theResults.getFrom().substring(8,10); //grabs the day
//        int dayOfOutageInt = Integer.parseInt(monthOfOutageStr);
////
////        //valid outage message - if response is a Date
//        if (currDate.after(theResults.getFrom()) && currDate.before(theResults.getTo())) {
//            return theResults.getText();
//        }
//        return null;



        //valid outage message - if response is a String
//        if (currDate.getMonthValue() == monthOfOutageInt && currDate.getDayOfMonth() >= dayOfOutageInt
//            && currDate.getYear() == yearOfOutageInt) {
//            return theResults.getText();
//        } else {
//            return null;
//        }

    }
}
