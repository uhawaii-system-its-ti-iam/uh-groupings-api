package edu.hawaii.its.api.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Hydrate an object as you see fit. GenericServiceResult is a class which will build a collection of arbitrary objects.
 */
public class GenericServiceResult {
    // Storage of arbitrary objects.
    ArrayList<Object> data;
    // Storage of names and indices of each objects added.
    HashMap<String, Integer> map;

    public GenericServiceResult() {
        this.data = new ArrayList<>();
        this.map = new HashMap<>();
    }

    /**
     * Initialize map and object list then call add.
     *
     * @param props   - list of corresponding name values.
     * @param objects - a variable amount of arbitrary objects.
     */
    public GenericServiceResult(List<String> props, Object... objects) {
        this.data = new ArrayList<>();
        this.map = new HashMap<>();
        this.add(props, objects);
    }

    /**
     * Add a variable amount of arbitrary objects, along with a list of their corresponding name values.
     * Example:
     * new GenericServiceResult(Arrays.asList("objA", "objB", "objC"), objA, objB, objC );
     *
     * @param props   - list of corresponding name values.
     * @param objects - a variable amount of arbitrary objects.
     */
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
