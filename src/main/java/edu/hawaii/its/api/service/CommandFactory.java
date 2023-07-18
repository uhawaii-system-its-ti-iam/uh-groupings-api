package edu.hawaii.its.api.service;

import edu.hawaii.its.api.wrapper.Command;

public class CommandFactory {

    Command returnZ  = () -> "Z";
    Command returnA = () -> "A";
    Command returnB = () -> "B";
    Command returnError = () -> "No such command exists";

    private Command executed;

    public Command create(String command) {

        switch (command) {
            case "returnZ":
                return returnZ;

            case "returnB":
                return returnB;

            case "returnA":
                return returnA;

            default: return returnError;
        }

    }



}
