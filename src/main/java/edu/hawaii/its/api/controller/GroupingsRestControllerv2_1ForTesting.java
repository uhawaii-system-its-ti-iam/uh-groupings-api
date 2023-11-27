package edu.hawaii.its.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.hawaii.its.api.exception.ExceptionForTesting;

@RestController
@RequestMapping("/api/groupings/v2.1/testing")
public class GroupingsRestControllerv2_1ForTesting {

    private static final Log logger = LogFactory.getLog(GroupingsRestControllerv2_1ForTesting.class);

    @GetMapping(value = "/exception")
    @ResponseBody
    public ResponseEntity throwException() {
        logger.debug("Entered REST throwException");
        throw new ExceptionForTesting("Test exception");
    }
}
