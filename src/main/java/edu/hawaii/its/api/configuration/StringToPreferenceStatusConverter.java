package edu.hawaii.its.api.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.PreferenceStatus;

import org.springframework.core.convert.converter.Converter;

public class StringToPreferenceStatusConverter implements Converter<String, PreferenceStatus> {

    private static final Log logger = LogFactory.getLog(StringToPreferenceStatusConverter.class);

    @Override
    public PreferenceStatus convert(String source) {
        PreferenceStatus preferenceStatus = PreferenceStatus.find(source);
        if (preferenceStatus == null) {
            logger.info("Error: Invalid PreferenceStatus value sent in to converter.");
        }
        return preferenceStatus;
    }

}
