package app;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }

    @RequestMapping("/hi")
    public String hi(){
        return "hi";
    }

    @RequestMapping("/groups")
    public WsGetGroupsResults wsGetGroupsResults(){

        return new GcGetGroups().addSubjectIdentifier("zknoebel").execute();

    }
    @RequestMapping("/members")
    public WsGetMembersResults wsGetMembersResults(){
        return new GcGetMembers().addGroupName("hawaii.edu:custom:test:zknoebel:zknoebel-test:basis+include").assignIncludeSubjectDetail(true).execute();
    }

}
