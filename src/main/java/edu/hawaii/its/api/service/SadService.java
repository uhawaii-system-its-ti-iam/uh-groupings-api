package edu.hawaii.its.api.service;

public class SadService implements MoodService {

    @Override
    public String state() {
        return "So sad. :(";
    }
}
