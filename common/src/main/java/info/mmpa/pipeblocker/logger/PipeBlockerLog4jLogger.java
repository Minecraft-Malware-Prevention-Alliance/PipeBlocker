package info.mmpa.pipeblocker.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PipeBlockerLog4jLogger implements PipeBlockerLogger {
    private final Logger logger = LogManager.getLogger("PipeBlocker");

    @Override
    public void info(String msg) {
        logger.info("{}", msg);
    }

    @Override
    public void error(String msg) {
        logger.error("{}", msg);
    }

    @Override
    public void debug(String msg) {
        logger.debug("{}", msg);
    }

    @Override
    public void warn(String msg) {
        logger.warn("{}", msg);
    }

    @Override
    public void fatal(String msg) {
        logger.fatal("{}", msg);
    }
}
