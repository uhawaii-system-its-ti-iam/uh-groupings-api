package edu.hawaii.its.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JsonUtil {
    private static final Log logger = LogFactory.getLog(JsonUtil.class);

    // Private constructor to prevent instantiation.
    private JsonUtil() {
        // Empty.
    }

    public static String asJson(final Object obj) {
        String result = null;
        try {
            result = new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Error: " + e);
            // Maybe we should throw something?
        }
        return result;
    }

    public static <T> T asObject(final String json, Class<T> type) {
        T result = null;
        try {
            result = new ObjectMapper().readValue(json, type);
        } catch (Exception e) {
            logger.error("Error: " + e);
            // Maybe we should throw something?
        }
        return result;
    }

    public void printJson(final Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = "{" + objectMapper.writeValueAsString(obj) + "}";
            System.err.println(json);
        } catch (JsonProcessingException e) {
            logger.error("Error: " + e);
        }
    }
}
