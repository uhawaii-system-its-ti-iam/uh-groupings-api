package edu.hawaii.its.api.service;

import org.springframework.stereotype.Service;

import edu.hawaii.its.api.wrapper.Command;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;

@Service("executor")
public class ExecutorService {
    protected final Log logger = LogFactory.getLog(getClass());

    public <T> T execute(Command<T> command) {
        T result = null;
        String text = "execute; " + command.getClass().getSimpleName() + ": ";
        try {
            result = command.execute();
            logger.debug(text + "execution success");
        } catch (Exception e) {
            logger.error(text + e);
        }
        return result;
    }
}
