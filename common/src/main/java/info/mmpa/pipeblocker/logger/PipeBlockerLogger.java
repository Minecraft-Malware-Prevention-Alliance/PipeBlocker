package info.mmpa.pipeblocker.logger;

public interface PipeBlockerLogger {
    void info(String msg);
    void debug(String msg);
    void warn(String msg);
    void fatal(String msg);
    void error(String msg);

    static PipeBlockerLogger detectLogger() {
        try {
            Class.forName("org.apache.logging.log4j.Logger");
            Class.forName("org.apache.logging.log4j.LogManager");
            return new PipeBlockerLog4jLogger();
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("java.util.logging.Logger");
            return new PipeBlockerJavaLogger();
        } catch (ClassNotFoundException ignored) {
        }

        // Should never reach here
        return new PipeBlockerStdoutLogger();
    }
}
