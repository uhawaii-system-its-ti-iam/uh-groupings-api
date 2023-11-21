package edu.hawaii.its.api.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;

import edu.hawaii.its.api.type.OptType;

public class StringToOptTypeConverter implements Converter<String, OptType> {

    private static final Log logger = LogFactory.getLog(StringToOptTypeConverter.class);

    @Override
    public OptType convert(String source) {
        OptType optType = OptType.find(source);
        if (optType == null) {
            logger.error("Error: Invalid OptType value sent in to converter.");
        }
        return optType;
    }

}
