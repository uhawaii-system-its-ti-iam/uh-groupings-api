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
