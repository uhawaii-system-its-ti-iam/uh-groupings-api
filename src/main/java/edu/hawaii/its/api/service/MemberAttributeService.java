package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;

import java.util.Map;

public interface MemberAttributeService {
    public GroupingsServiceResult assignOwnership(String groupingPath, String ownerUsername, String newOwnerUsername);

    public GroupingsServiceResult removeOwnership(String groupingPath, String username, String ownerToRemoveUsername);

    public boolean isMember(String groupPath, String username);

    public boolean isMember(String groupPath, Person person);

    public boolean isMemberUuid(String groupPath, String idnum);

    public boolean isOwner(String groupingPath, String username);

    public boolean isAdmin(String username);

    public boolean isApp(String username);

    public boolean isSuperuser(String username);

    public boolean isSelfOpted(String groupPath, String username);

    public WsAttributeAssign[] getMembershipAttributes(String assignType, String attributeUuid, String membershipID);

    public Map<String, String> getUserAttributes(String ownerUsername, String username) throws GcWebServiceError;

    public Map<String, String> getUserAttributesLocal(String username);

}
