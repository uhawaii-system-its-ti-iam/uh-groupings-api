package edu.hawaii.its.api.wrapper;

public class MockCommand extends GrouperCommand<MockCommand> implements Command<MockResults> {

    @Override
    public MockCommand self() {
        return this;
    }

    @Override
    public MockResults execute() {
        return new MockResults("SUCCESS");
    }

}
