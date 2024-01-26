package edu.hawaii.its.api.service;

import org.springframework.stereotype.Service;

import edu.hawaii.its.api.wrapper.Command;
import edu.hawaii.its.api.wrapper.Results;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;

@Service
public class ExecutorService {
    protected final Log logger = LogFactory.getLog(getClass());

    public <T extends Results> T execute(Command<T> command) {
        String text = "execute; " + command.getClass().getSimpleName() + ": ";
        try {
            T result = command.execute();
            logger.debug(text + "execution success");
            return result;
        } catch (Exception e) {
            logger.error(text + e);
        }
        return null;
    }
}
