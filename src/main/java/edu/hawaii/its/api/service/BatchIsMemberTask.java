package edu.hawaii.its.api.service;
import edu.hawaii.its.api.type.Person;

import java.util.concurrent.Callable;

public class BatchIsMemberTask implements Callable<Boolean> {

    private final String groupPath;
    private final Person personToAdd;
    private final BatchIsMember bd;

    public BatchIsMemberTask(String groupPath, Person personToAdd, BatchIsMember bd) {
        this.groupPath = groupPath;
        this.personToAdd = personToAdd;
        this.bd = bd;
    }

    @Override
    public Boolean call() {
        return bd.isMember(groupPath, personToAdd);
    }
}

class BatchIsOwnerTask implements Callable<Boolean> {

    private final String groupPath;
    private final String personToAdd;
    private final BatchIsMember bd;

    public BatchIsOwnerTask(String groupPath, String personToAdd, BatchIsMember bd) {
        this.groupPath = groupPath;
        this.personToAdd = personToAdd;
        this.bd = bd;
    }

    @Override
    public Boolean call() {
        return bd.isOwner(groupPath, personToAdd);
    }
}

class BatchIsSuperUserTask implements Callable<Boolean> {
    private final String username;
    private final BatchIsMember bd;

    public BatchIsSuperUserTask(String username, BatchIsMember bd) {
        this.username = username;
        this.bd = bd;
    }

    @Override
    public Boolean call() {
        return bd.isSuperuser(username);
    }
}