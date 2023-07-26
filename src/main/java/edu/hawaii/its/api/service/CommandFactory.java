package edu.hawaii.its.api.service;

import edu.hawaii.its.api.wrapper.Command;

public class CommandFactory {

    Command returnZ  = () -> "Z";
    Command returnA = () -> "A";
    Command returnB = () -> "B";
    Command returnError = () -> "No such command exists";
    Command returnNullPointerException =() -> {String str = null; return str.length(); };

    Command returnArithmeticException = () -> 10%0;

    private Command executed;

    public Command create(String command) {

        switch (command) {
            case "returnZ":
                return returnZ;

            case "returnB":
                return returnB;

            case "returnA":
                return returnA;

            case "returnNullPointerException":
                return returnNullPointerException;

            default: return returnError;
        }

    }



}
