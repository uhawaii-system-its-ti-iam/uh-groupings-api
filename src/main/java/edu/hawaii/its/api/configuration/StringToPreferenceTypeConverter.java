package edu.hawaii.its.api.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.PreferenceType;

import org.springframework.core.convert.converter.Converter;

public class StringToPreferenceTypeConverter implements Converter<String, PreferenceType> {

    private static final Log logger = LogFactory.getLog(StringToPreferenceTypeConverter.class);

    @Override
    public PreferenceType convert(String source) {
        PreferenceType preferenceType = PreferenceType.find(source);
        if (preferenceType == null) {
            logger.info("Error: Invalid PreferenceType value sent in to converter.");
        }
        return preferenceType;
    }

}
