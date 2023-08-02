package info.mmpa.pipeblocker;

import org.apache.logging.log4j.*;

import java.lang.instrument.*;

public class PipeBlockerAgent {
    public static void premain(String args, Instrumentation instrumentation){
        PipeBlocker.apply();
        LogManager.getLogger("PipeBlocker").info("PipeBlocker Java agent loaded.");
    }
}
