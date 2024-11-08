package edu.hawaii.its.api.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;

import edu.hawaii.its.api.type.SortBy;

public class StringToSortByConverter implements Converter<String, SortBy> {

    private static final Log logger = LogFactory.getLog(StringToSortByConverter.class);

    @Override
    public SortBy convert(String source) {
        SortBy sortBy = SortBy.find(source);
        if (sortBy == null) {
            logger.error("Error: Invalid SortBy value sent in to converter.");
        }
        return sortBy;
    }

}
