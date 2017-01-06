package App.controller;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetGroupsController {




    @RequestMapping("/groups")
    public edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults wsGetGroupsResults(){

        return new GcGetGroups().addSubjectIdentifier("zknoebel").execute();

     }
}
