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
        LocalDateTime currDate = LocalDateTime.now();
        //the whole response from Grouper: a list of objects
//        PlannedOutageResult[] theResults = new PlannedOutageResult();
//        PlannedOutageResult theResults = new PlannedOutageResult().GetPlannedOutageResult("path/to/Grouper");
        List<Map<String, String>> messages = Arrays.asList(
                new HashMap<String, String>() {{
                    put("message", "text to display during date range");
                    put("from", "20230901T000000");
                    put("to", "20230903T000000");
                }},
                new HashMap<String, String>() {{
                    put("message", "text to display during date range given by from and to below");
                    put("from", "20230901T000000");
                    put("to", "20230903T000000");
                }}
        );

        for (int i = 0; i < messages.size(); i++) {
            //convert dates here
            LocalDateTime dateTo = Dates.toLocalDateTime(messages.get(i).get("to"), Dates.DATE_FORMAT_PLANNEDOUTAGE);
            LocalDateTime dateFrom = Dates.toLocalDateTime( messages.get(i).get("from"), Dates.DATE_FORMAT_PLANNEDOUTAGE);

            if (dateFrom.isBefore(currDate) && dateTo.isAfter(currDate)) {
                return messages.get(i).get("message");
            }
        }
        return null;
    }
}
