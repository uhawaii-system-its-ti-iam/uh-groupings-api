package edu.hawaii.its.api.wrapper;

@FunctionalInterface
public interface Command<T> {
    T execute();
}
