package edu.hawaii.its.api.service;

import edu.hawaii.its.api.wrapper.Command;

public class CommandFactory {

    Command zCommand  = () -> "Z";
    Command aCommand = () -> "A";
    Command bCommand = () -> "B";
    Command errorCommand = () -> "No such command exists";
    Command nullPointerExceptionCommand =() -> {throw new NullPointerException();};

    Command arithmeticExceptionCommand = () -> {throw new ArithmeticException();};

    private Command executed;

    public Command create(String command) {

        switch (command) {
            case "zCommand":
                return zCommand;

            case "bCommand":
                return bCommand;

            case "aCommand":
                return aCommand;

            case "nullPointerExceptionCommand":
                return nullPointerExceptionCommand;

            case "arithmeticExceptionCommand":
                return arithmeticExceptionCommand;

            default: return errorCommand;
        }

    }



}
