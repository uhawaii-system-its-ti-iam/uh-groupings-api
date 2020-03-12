package edu.hawaii.its.api.type;

import java.util.ArrayList;

public class GenericServiceResult {
    ArrayList<Object> data;

    public GenericServiceResult() {
        this.data = new ArrayList<>();
    }

    GenericServiceResult(Object obj) {
        this.data = new ArrayList<>();
        this.add(obj);
    }

    public ArrayList<Object> add(Object obj) {
        this.data.add(obj);
        return this.data;
    }

    public ArrayList<Object> addList(Object... objs) {
        for (Object obj : objs)
            this.add(obj);
        return this.data;
    }

    public ArrayList<Object> getData() {
        return this.data;
    }
}
