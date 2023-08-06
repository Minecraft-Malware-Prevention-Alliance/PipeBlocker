package info.mmpa.pipeblocker.logger;

public class PipeBlockerStdoutLogger implements PipeBlockerLogger {
    @Override
    public void info(String msg) {
        System.out.println("[PipeBlocker] [INFO] " + msg);
    }

    @Override
    public void error(String msg) {
        System.out.println("[PipeBlocker] [ERROR] " + msg);
    }

    @Override
    public void debug(String msg) {
        System.out.println("[PipeBlocker] [DEBUG] " + msg);
    }

    @Override
    public void warn(String msg) {
        System.out.println("[PipeBlocker] [WARN] " + msg);
    }

    @Override
    public void fatal(String msg) {
        System.out.println("[PipeBlocker] [FATAL] " + msg);
    }
}
