package edu.hawaii.its.api.service;

public class HappyService implements MoodService {
    @Override
    public String state() {
        return "I am happy!";
    }
}
