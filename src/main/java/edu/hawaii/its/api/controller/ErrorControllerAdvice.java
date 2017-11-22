package edu.hawaii.its.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ErrorControllerAdvice {

    private static final Log logger = LogFactory.getLog(GroupingsRestController.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handelIllegalArgumentException(IllegalArgumentException iae) {
        logger.error("Exception: " + iae.getCause());

        ModelAndView modelAndView = new ModelAndView("/error");
        modelAndView.addObject("errCode", 500);
        modelAndView.addObject("errMsg", iae.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {
        return "redirect:/";
    }

}