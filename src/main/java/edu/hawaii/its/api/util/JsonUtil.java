package edu.hawaii.its.api.util;

import java.nio.file.Files;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        }
        return result;
    }

    public static <T> T asObject(final String json, Class<T> type) {
        T result = null;
        try {
            result = new ObjectMapper().readValue(json, type);
        } catch (Exception e) {
            logger.error("Error: " + type + "; " + e);
        }
        return result;
    }

    public static <T> List<T> asList(final String json, Class<T> type) {
        List<T> result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (Exception e) {
            logger.error("Error: " + e);
        }
        return result;
    }

    private static String readJsonFileToString(String filePath) {
        String result = null;
        try {
            if (!filePath.endsWith(".json")) {
                throw new IllegalArgumentException("The provided file is not a JSON file: " + filePath);
            }
            Resource resource = new ClassPathResource(filePath);
            result = Files.readString(resource.getFile().toPath());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid file error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error: " + e);
        }
        return result;
    }

    public static <T> List<T> asListFromFile(final String filePath, Class<T> type) {
        return JsonUtil.asList(readJsonFileToString(filePath), type);
    }

    public static <T> T asObjectFromFile(final String filePath, Class<T> type) {
        return JsonUtil.asObject(readJsonFileToString(filePath), type);
    }

    public static void printJson(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(obj);
            System.err.println(json);
        } catch (Exception e) {
            logger.error("Error: " + e);
        }
    }

    public static void prettyPrint(Object object) {
        try {
            String json = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(object);
            System.out.println(json);
        } catch (Exception e) {
            logger.error("Error: " + e);
        }
    }
}