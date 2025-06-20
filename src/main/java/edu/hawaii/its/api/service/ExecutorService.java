package edu.hawaii.its.api.service;

import org.springframework.stereotype.Service;

import edu.hawaii.its.api.wrapper.GrouperCommand;
import edu.hawaii.its.api.wrapper.Command;
import edu.hawaii.its.api.wrapper.Results;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;

import edu.hawaii.its.api.exception.GrouperException;

@Service
public class ExecutorService {
    protected final Log logger = LogFactory.getLog(getClass());

    private static final int MAX_RETRIES = 2;
    private static final int DELAY = 1000;

    public <T extends Results> T execute(Command<T> command) {

        boolean retry = false;

        if (command instanceof GrouperCommand) {
            retry = ((GrouperCommand<?>) command).isRetry();
        }

        String text = "execute; " + command.getClass().getSimpleName() + ": ";
        T result = null;
        Exception ex = null;

        for (int i = 0; i <= MAX_RETRIES; i++) {
            try {
                result = command.execute();
                if (result.getResultCode().startsWith("SUCCESS")) {
                    logger.debug(text + "execution success");
                    return result;
                }
            } catch (Exception e) {
                logger.error(text + e);
                ex = e;
            }

            if (!retry || i == MAX_RETRIES) {
                if (ex != null) {
                    throw new GrouperException("GrouperClient execution failed: %s" + command.getClass().getSimpleName(), ex);
                }
                break;
            }

            ex = null;
            logger.info(text + "Execution failed, retrying in " + DELAY * i + " ms");
            delay(i);
        }
        return result;
    }

    protected void delay(int i) {
        try {
            Thread.sleep((long) DELAY * i);
        } catch (InterruptedException ie) {
            logger.error(ie);
        }
    }
}
