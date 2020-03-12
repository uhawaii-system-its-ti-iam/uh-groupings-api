package edu.hawaii.its.api.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GenericServiceResult {
    HashMap<String, Integer> map;
    ArrayList<Object> data;

    public GenericServiceResult() {
        this.data = new ArrayList<>();
        this.map = new HashMap<>();
    }

    public GenericServiceResult(List<String> props, Object... objects) {
        this.data = new ArrayList<>();
        this.map = new HashMap<>();
        this.add(props, objects);
    }

    public ArrayList<Object> add(List<String> props, Object... objects) {
        int i = 0;
        for (Object object : objects) {
            this.map.put(props.get(i), i);
            this.data.add(object);
            i++;
        }
        return this.data;
    }

    public ArrayList<Object> getData() {
        return this.data;
    }

    public HashMap<String, Integer> getMap() {
        return this.map;
    }
}
