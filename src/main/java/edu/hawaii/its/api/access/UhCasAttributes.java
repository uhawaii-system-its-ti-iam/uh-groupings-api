package edu.hawaii.its.api.access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UhCasAttributes implements UhAttributes {

    private Map<String, List<String>> uhAttributeMap = new HashMap<>();
    private final String username; // CAS login username.
    private final Map<?, ?> map; // Original CAS results.

    public UhCasAttributes() {
        this(new HashMap<>());
    }

    public UhCasAttributes(Map<?, ?> map) {
        this("", map);
    }

    public UhCasAttributes(String username, Map<?, ?> map) {
        this.username = username != null ? username : "";
        this.map = map;
        if (map != null) {
            for (Object key : map.keySet()) {
                if (key != null && key instanceof String) {
                    String k = ((String) key).toLowerCase();
                    Object v = map.get(key);
                    if (v != null) {
                        if (v instanceof String) {
                            uhAttributeMap.put(k, Arrays.asList((String) v));
                        } else if (v instanceof List) {
                            List<String> lst = new ArrayList<>();
                            for (Object o : (List<?>) v) {
                                if (o != null && o instanceof String) {
                                    lst.add((String) o);
                                }
                            }
                            uhAttributeMap.put(k, lst);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return getValue("cn");
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getUid() {
        List<String> values = uhAttributeMap.get("uid");
        if (null == values) {
            return "";
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        // If theres more than one uid in the results, try to match one with username.
        if (values.size() > 1) {
            for (String s : values) {
                if (s.equals(getUsername())) {
                    return s;
                }
            }
        }
        // If couldn't match up username with uid, just return the first value.
        return "";
    }

    @Override
    public String getUhUuid() {
        return getValue("uhUuid");
    }

    @Override
    public List<String> getMail() {
        return getValues("mail");
    }

    @Override
    public List<String> getAffiliation() {
        return getValues("eduPersonAffiliation");
    }

    @Override
    public List<String> getValues(String name) {
        List<String> results = uhAttributeMap.get(toLowerCase(name));
        if (results != null) {
            return Collections.unmodifiableList(results);
        }
        return Collections.emptyList();
    }

    @Override
    public String getValue(String name) {
        List<String> results = getValues(name);
        return results.isEmpty() ? "" : results.get(0);
    }

    @Override
    public Map<?, ?> getMap() {
        return Collections.unmodifiableMap(map);
    }

    private String toLowerCase(String s) {
        return (s != null) ? s.toLowerCase() : s;
    }

    @Override
    public String toString() {
        return "UhCasAttributes [username=" + username
                + ", uhAttributeMap=" + uhAttributeMap
                + ", map=" + map + "]";
    }

}
