package edu.hawaii.its.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.hawaii.its.api.service.OotbGroupingPropertiesService;
import edu.hawaii.its.api.type.OotbActiveProfile;
import edu.hawaii.its.api.type.OotbActiveProfileResult;

@RestController
@Profile("ootb")
@RequestMapping("/api/groupings/v2.1")
public class OotbRestController {
    private static final Log logger = LogFactory.getLog(OotbRestController.class);

    private final OotbGroupingPropertiesService ootbGroupingPropertiesService;

    // Constructor
    public OotbRestController(OotbGroupingPropertiesService ootbGroupingPropertiesService) {
        this.ootbGroupingPropertiesService = ootbGroupingPropertiesService;
    }

     /**
     * Update Ootb active user profile.
     */
     @PostMapping(value = "/activeProfile/ootb")
     public ResponseEntity<OotbActiveProfileResult> updateOotbActiveUserGroupings(@RequestBody
     OotbActiveProfile ootbActiveProfile) {
        logger.debug("Entered REST updateOotbActiveUserGroupings...");
        return ResponseEntity
                .ok()
                .body(ootbGroupingPropertiesService.updateActiveUserProfile(ootbActiveProfile));
     }
}
