package info.mmpa.pipeblocker;

import org.apache.logging.log4j.*;

import java.lang.instrument.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import java.lang.*;

public class PipeBlockerAgent {
    private static final ArrayBlockingQueue<ClassReport> queue = new ArrayBlockingQueue<>(200);
    private static final HashSet<String> classesReported = new HashSet<>();
    private static final File pb = new File("./.pipeblocker_accessed.txt");
    private static final FileOutputStream fos;
    
    static {
        try {
            fos = new FileOutputStream(pb);
            fos.getChannel().truncate(0).close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void premain(String args, Instrumentation instrumentation){
        PipeBlocker.setLogOnly(false);
        PipeBlocker.setFilterHook((underlyingClass, matchType, status) -> {
            String className = underlyingClass.getCanonicalName();
            if (!classesReported.contains(className)) {
                classesReported.add(className);
                try {
                    queue.offer(new ClassReport(className, matchType), 5, TimeUnit.SECONDS);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return status; // do not touch the status
        });
        PipeBlocker.setFilterURL("https://pb-mfnxqeo.semisol.net/filter.txt");
        PipeBlocker.apply();
        new Thread(() -> {
            while (true) {
                ClassReport cr;
                try {
                    cr = queue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                try {
                    fos.write((cr.className + " " + cr.matchType + "\n").getBytes());
                } catch (Exception e) { e.printStackTrace(); }
            }
        }).start();
        LogManager.getLogger("PipeBlocker").info("PipeBlocker Java agent loaded.");
    }
    private static class ClassReport {
        public final String className;
        public final FilterMatchType matchType;
        ClassReport(String className, FilterMatchType matchType) {
            this.className = className;
            this.matchType = matchType;
        }
    }
}
