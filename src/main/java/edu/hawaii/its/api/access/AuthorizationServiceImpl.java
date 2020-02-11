package edu.hawaii.its.api.access;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.MemberAttributeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    private Map<String, List<Role>> userMap = new HashMap<>();

    //HELLO

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    private static final Log logger = LogFactory.getLog(AuthorizationServiceImpl.class);

    /**
     * Assigns roles to user
     *
     * @param uhUuid   : The uhUuid of the user.
     * @param username : The username of the person to find the user.
     * @return : Returns an array list of roles assigned to the user.
     */
    @Override
    public RoleHolder fetchRoles(String uhUuid, String username) {
        RoleHolder roleHolder = new RoleHolder();
        roleHolder.add(Role.ANONYMOUS);
        roleHolder.add(Role.UH);

        //Determines if user is an owner.
        if (isOwner(username)) {
            roleHolder.add(Role.OWNER);
        }

        //Determines if a user is an admin.
        if (isAdmin(username)) {
            roleHolder.add(Role.ADMIN);
        }

        List<Role> roles = userMap.get(uhUuid);
        if (roles != null) {
            for (Role role : roles) {
                roleHolder.add(role);
            }
        }
        return roleHolder;
    }

    /**
     * Determines if a user is an owner of any grouping.
     *
     * @param username - self-explanitory
     * @return true if the person has groupings that they own, otherwise false.
     */
    public boolean isOwner(String username) {
        try {
            logger.info("//////////////////////////////");
            if (!groupingAssignmentService.getGroupingAssignment(username).getGroupingsOwned().isEmpty()) {
                logger.info("This person is an owner");
                return true;
            } else {
                logger.info("This person is not owner");
            }
        } catch (Exception e) {
            logger.info("The grouping for this person is " + e.getMessage());
        }
        return false;
    }

    /**
     * Determines if a user is an admin in grouping admin.
     *
     * @param username - self-explanitory
     * @return true if the person gets pass the grouping admins check by checking if they can get all the groupings.
     */
    public boolean isAdmin(String username) {
        logger.info("//////////////////////////////");
        try {
            if (memberAttributeService.isAdmin(username)) {
                logger.info("this person is an admin");
                return true;
            } else {
                logger.info("this person is not an admin");
            }
        } catch (Exception e) {
            logger.info("Error in getting admin info. Error message: " + e.getMessage());
        }
        logger.info("//////////////////////////////");
        return false;
    }
}
