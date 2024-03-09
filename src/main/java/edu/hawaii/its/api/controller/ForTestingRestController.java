package edu.hawaii.its.api.controller;

import edu.hawaii.its.api.service.OotbGroupingPropertiesService;
import edu.hawaii.its.api.type.ApiValidationError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.hawaii.its.api.exception.ExceptionForTesting;

@RestController
@RequestMapping("/api/groupings/v2.1/testing")
public class ForTestingRestController {

    private static final Log logger = LogFactory.getLog(ForTestingRestController.class);

    @Autowired
    OotbGroupingPropertiesService ootbGroupingPropertiesService;

    @GetMapping(value = "/exception")
    @ResponseBody
    public ResponseEntity throwException() {
        logger.debug("Entered REST throwException");
        ExceptionForTesting exception = new ExceptionForTesting("Exception for test failed")
                .addSubError(new ApiValidationError("testing object 1", "membership service", "id: 12", "Membership access denied"))
                .addSubError(new ApiValidationError("testing object 2", "member service", "id: 30", "There is no member"));
        throw exception;
    }

}
