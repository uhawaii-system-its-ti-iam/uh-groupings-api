package edu.hawaii.its.api.type;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Hydrate an object as you see fit. GenericServiceResult is a class which will build a collection of arbitrary objects.
 */
public class GenericServiceResult {
    // Storage of arbitrary objects.
    ArrayList<Object> data;
    // Storage of names and indices of each objects added.
    HashMap<String, Integer> keys;
    int size;

    public GenericServiceResult() {
        this.data = new ArrayList<>();
        this.keys = new HashMap<>();
        this.size = 0;
    }

    /**
     * Initialize map and object list then call add.
     *
     * @param props   - list of corresponding name values.
     * @param objects - a variable amount of arbitrary objects.
     */
    public GenericServiceResult(List<String> props, Object... objects) {
        this();
        this.add(props, objects);
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
        for (Object object : objects) {
            this.keys.put(keys.get(this.size), this.size);
            this.data.add(object);
            this.size++;
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
        this.keys.put(key, this.size);
        this.size++;
    }

    public boolean remove(String key) {
        return this.data.remove(this.keys.remove(key));
    }

    public int size() {
        return this.size;
    }

    public String prettyString() {
        String str = "";
        Set<String> keys = this.keys.keySet();
        for (String temp : keys) {
            str += temp + "=" + this.data.get(this.keys.get(temp)) + ", ";
        }
        return str;
    }

    @Override
    public String toString() {
        String str = "GenericServiceResult{data=[";

        for (Object object : this.data) {
            str += object.toString();
        }
        str += "], keys={" + this.keys.toString() + "}}, "
                + "ParsedGenericServiceResult{"
                + this.prettyString() + "}";
        return str;
    }

    public ArrayList<Object> getData() {
        return this.data;
    }

    public HashMap<String, Integer> getKeys() {
        return this.keys;
    }

    public Object get(String key) {
        try {
            return getData().get(getKeys().get(key));
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(e.getMessage());
        }
    }
}
