package APICalls;

/**
 * Created by zac on 12/15/16.
 */
public class AssignPrivilege extends GrouperFunction{
    private String privilege;

    public AssignPrivilege(String userName, String grouping, String privilege) {
        super(userName, grouping);
        this.privilege = privilege;
    }


}
