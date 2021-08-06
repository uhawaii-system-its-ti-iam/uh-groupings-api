package edu.hawaii.its.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.Locale;

//This home controller is only for the API side of UH Groupings. Do Not Add Anything Else!
@Controller
public class HomeController {
    private static final Log logger = LogFactory.getLog(HomeController.class);

    @GetMapping(value = { "/" })
    public String home(Locale locale) {
        logger.info("User at home. The client locale is " + locale);
        return "home";
    }


}
