package info.mmpa.pipeblocker;

import java.lang.instrument.*;

public class PipeBlockerAgent {
    public static void premain(String args, Instrumentation instrumentation){
        PipeBlocker.chooseBestLogger();
        PipeBlocker.apply();
        System.out.println("PipeBlocker Java agent loaded.");
    }
}
