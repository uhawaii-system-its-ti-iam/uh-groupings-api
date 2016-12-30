package MainTest;

import APICalls.*;
import App.GroupingsClient;
import GroupingsFunctions.*;

public class Main{
    public static void main(String[] args){

        GroupingsClient groupingsClient = new GroupingsClient("_api_groupings", "hawaii.edu:custom:test:zknoebel:zknoebel-test");
        groupingsClient.deleteMember("zknoebel");
        groupingsClient.addMember("zknoebel");
//        groupingsClient.groupingsImIn();
//        groupingsClient.getMembers();
//        groupingsClient.getOwners();
    }
}
