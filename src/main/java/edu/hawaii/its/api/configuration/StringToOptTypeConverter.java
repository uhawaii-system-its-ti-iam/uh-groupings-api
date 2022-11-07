package edu.hawaii.its.api.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.OptType;

import org.springframework.core.convert.converter.Converter;

public class StringToOptTypeConverter implements Converter<String, OptType> {

    private static final Log logger = LogFactory.getLog(StringToOptTypeConverter.class);

    @Override
    public OptType convert(String source) {
        OptType optType = OptType.find(source);
        if (optType == null) {
            logger.info("Error: Invalid OptType value sent in to converter.");
        }
        return optType;
    }

}
