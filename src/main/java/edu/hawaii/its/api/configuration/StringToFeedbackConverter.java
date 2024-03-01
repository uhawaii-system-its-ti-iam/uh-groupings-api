package edu.hawaii.its.api.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;

import edu.hawaii.its.api.type.Feedback;
import edu.hawaii.its.api.util.JsonUtil;

public class StringToFeedbackConverter implements Converter<String, Feedback>  {

    private static final Log logger = LogFactory.getLog(StringToFeedbackConverter.class);

    @Override
    public Feedback convert(String source) {
        Feedback feedback = JsonUtil.asObject(source, Feedback.class);
        if (feedback == null) {
            logger.error("Error: Invalid Feedback value sent in to converter.");
        }
        return feedback;
    }

}
