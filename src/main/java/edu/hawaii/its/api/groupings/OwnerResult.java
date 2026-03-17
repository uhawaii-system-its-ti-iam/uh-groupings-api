package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;

public class OwnerResult {

    private final String uhUuid;
    private final String name;
    private final String uid;
    private final List<String> paths = new ArrayList<>();

    public OwnerResult(String uhUuid, String name, String uid, String initialPath) {
        this.uhUuid = uhUuid;
        this.name = name;
        this.uid = uid;
        this.paths.add(initialPath);
    }

    public String getUhUuid() {
        return uhUuid;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void addPath(String path) {
        this.paths.add(path);
    }
}

