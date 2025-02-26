package edu.hawaii.its.api.service;

import org.springframework.stereotype.Service;

import edu.hawaii.its.api.wrapper.Command;
import edu.hawaii.its.api.wrapper.Results;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;

@Service
public class ExecutorService {
    protected final Log logger = LogFactory.getLog(getClass());

    private static final int MAX_RETRIES = 2;
    private static final int DELAY = 1000;

    public <T extends Results> T execute(boolean retry, Command<T> command) {
        String text = "execute; " + command.getClass().getSimpleName() + ": ";
        for (int i = 0; i <= MAX_RETRIES; i++) {
            try {
                T result = command.execute();
                if (result.getResultCode().startsWith("SUCCESS")) {
                    logger.debug(text + "execution success");
                    return result;
                }
                if (!retry || i == MAX_RETRIES)
                    return result;
            } catch (Exception e) {
                logger.error(text + e);
            }
            if (!retry)
                return null;
            logger.debug(text + "Execution failed, retrying in " + DELAY * i + " ms");
            delay(i);
        }
        return null;
    }

    protected void delay(int i) {
        try {
            Thread.sleep((long) DELAY * i);
        } catch (InterruptedException ie) {
            logger.error(ie);
        }
    }
}
