package info.mmpa.pipeblocker.logger;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class PipeBlockerJavaLogger implements PipeBlockerLogger {
    private final Logger logger = Logger.getLogger("PipeBlocker");

    public PipeBlockerJavaLogger() {
        // Legacy FML logger injection
        try {
            Class<?> fmlLog = Class.forName("cpw.mods.fml.common.FMLLog");
            Method getLogger = fmlLog.getMethod("getLogger");
            getLogger.setAccessible(true);
            logger.setParent((Logger) getLogger.invoke(null));
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void error(String msg) {
        logger.severe(msg);
    }

    @Override
    public void debug(String msg) {
        logger.finest(msg);
    }

    @Override
    public void warn(String msg) {
        logger.warning(msg);
    }

    @Override
    public void fatal(String msg) {
        logger.severe(msg);
    }
}
