package edu.hawaii.its.api.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hydrate an object as you see fit. GenericServiceResult is a class
 * which will build a collection of arbitrary objects.
 */
public class GenericServiceResult {
    /* Storage of arbitrary objects. */
    List<Object> data;
    /* Storage of names and indices of each objects added. */
    Map<String, Integer> map;
    GroupingsServiceResult groupingsServiceResult;

    public GenericServiceResult() {
        this.data = new ArrayList<>();
        this.map = new HashMap<>();
        this.groupingsServiceResult = new GroupingsServiceResult();
    }

    public GenericServiceResult(GroupingsServiceResult groupingsServiceResult) {
        this.data = new ArrayList<>();
        this.map = new HashMap<>();
        this.groupingsServiceResult = groupingsServiceResult;
    }

    public GenericServiceResult(String key, Object object) {
        this();
        this.add(key, object);
    }

    public GenericServiceResult(GroupingsServiceResult groupingsServiceResult, String key, Object object) {
        this(groupingsServiceResult);
        this.add(key, object);
    }

    public GenericServiceResult(List<String> keys, Object... objects) {
        this();
        this.add(keys, objects);
    }

    public GenericServiceResult(GroupingsServiceResult groupingsServiceResult, List<String> keys, Object... objects) {
        this(groupingsServiceResult);
        this.add(keys, objects);
    }

    /**
     * Add a variable amount of arbitrary objects, along with a list of their corresponding name values.
     * Example:
     * new GenericServiceResult(Arrays.asList("objA", "objB", "objC"), objA, objB, objC );
     *
     * @param keys    - list of corresponding name values.
     * @param objects - a variable amount of arbitrary objects.
     */
    public void add(List<String> keys, Object... objects) {
        Iterator<String> iter = keys.iterator();
        for (Object object : objects) {
            this.add(iter.next(), object);
        }
    }

    /**
     * Add a single object and key to response.
     *
     * @param key    a single key.
     * @param object a single arbitrary object.
     */
    public void add(String key, Object object) {
        this.data.add(object);
        this.map.put(key, this.data.indexOf(object));
    }

    /**
     * Get a object by key.
     *
     * @param key which was assigned, when object was added.
     * @return object that was added with key.
     */
    public Object get(String key) {
        return this.data.get(this.map.get(key));
    }

    /**
     * @return List<Object>data
     */
    public List<Object> getData() {
        return Collections.unmodifiableList(this.data);
    }

    /**
     * @return Map<String, Integer>map
     */
    public Map<String, Integer> getMap() {
        return Collections.unmodifiableMap(this.map);
    }

    public GroupingsServiceResult getGroupingsServiceResult() {
        return groupingsServiceResult;
    }

    @Override
    public String toString() {
        Set<String> keys = this.map.keySet();
        Iterator<String> iter = keys.iterator();
        List<String> strs = new ArrayList<>();
        while (iter.hasNext()) {
            String key = iter.next();
            strs.add(key + ": " + this.get(key) + "; ");
        }
        return strs.toString();
    }
}
