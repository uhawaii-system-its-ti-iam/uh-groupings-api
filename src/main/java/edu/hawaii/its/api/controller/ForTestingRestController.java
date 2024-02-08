package edu.hawaii.its.api.controller;

import edu.hawaii.its.api.groupings.GroupingMember;
import edu.hawaii.its.api.service.GroupingPropertiesService;
import edu.hawaii.its.api.type.ApiValidationError;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.hawaii.its.api.exception.ExceptionForTesting;

import java.util.List;

@RestController
@RequestMapping("/api/groupings/v2.1/testing")
public class ForTestingRestController {

    private static final Log logger = LogFactory.getLog(ForTestingRestController.class);

    @Autowired
    GroupingPropertiesService groupingPropertiesService;

    @GetMapping(value = "/exception")
    @ResponseBody
    public ResponseEntity throwException() {
        logger.debug("Entered REST throwException");
        ExceptionForTesting exception = new ExceptionForTesting("Exception for test failed")
                .addSubError(new ApiValidationError("testing object 1", "membership service", "id: 12", "Membership access denied"))
                .addSubError(new ApiValidationError("testing object 2", "member service", "id: 30", "There is no member"));
        throw exception;
    }

//    @GetMapping(value = "/member")
//    @ResponseBody
//    public ResponseEntity<GroupingMember> memberBeanTest() {
//        logger.debug("Entered member Bean Test");
//        return ResponseEntity
//                .ok()
//                .body(groupingPropertiesService.getGroupingMemberBean());
//    }
//
//    @GetMapping(value = "/members")
//    @ResponseBody
//    public ResponseEntity<List<GroupingMember>> membershipsBeanTest() {
//        logger.debug("Entered memberships Bean Test");
//        return ResponseEntity
//                .ok()
//                .body(groupingPropertiesService.getGroupingMembersBean());
//    }
//
//    @GetMapping(value = "/hasMember")
//    @ResponseBody
//    public ResponseEntity<HasMembersResults> hasMemberResultBeanTest() {
//        logger.debug("Entered memberships Bean Test");
//        return ResponseEntity
//                .ok()
//                .body(groupingPropertiesService.getHasMembersResultsBean());
//    }
}
