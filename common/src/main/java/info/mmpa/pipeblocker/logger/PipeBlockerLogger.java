package info.mmpa.pipeblocker.logger;

public interface PipeBlockerLogger {
    void info(String msg);
    void debug(String msg);
    void warn(String msg);
    void fatal(String msg);
    void error(String msg);
}
