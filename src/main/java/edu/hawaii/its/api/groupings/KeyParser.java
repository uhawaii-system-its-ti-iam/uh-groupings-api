package edu.hawaii.its.api.groupings;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Helper class to format GroupingSyncDestination description.
 */
public class KeyParser {
    private final Pattern regex;

    private final String googleSyncDestSuffix;

    public KeyParser(String googleSyncDestSuffix, Pattern regex) {
        this.googleSyncDestSuffix = googleSyncDestSuffix;
        this.regex = regex;
    }

    public void replaceRegex(List<GroupingSyncDestination> list, String replace) {
        list.stream().filter(e -> regex.matcher(e.getDescription()).find()).forEach(syncDest -> {
            String newDescription = regex.matcher(syncDest.getDescription()).replaceAll(replace);
            if (syncDest.getName().contains("google-group")) {
                newDescription += googleSyncDestSuffix;
            }
            syncDest.setDescription(newDescription);
        });
    }
}
