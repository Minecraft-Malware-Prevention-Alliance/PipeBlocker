package info.mmpa.pipeblocker;

import org.apache.logging.log4j.*;

import java.lang.instrument.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import java.lang.*;

public class PipeBlockerAgent {
    private static final HashSet<String> classesReported = new HashSet<>();
    private static final File pb = new File("./.pipeblocker_accessed.txt");

    private static void truncateFile() {
        try {
            FileOutputStream fos = new FileOutputStream(pb);
            fos.getChannel().truncate(0).close();
            fos.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeString(String str) {
        try {
            FileOutputStream fos = new FileOutputStream(pb);
            fos.write((str + "\n").getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void premain(String args, Instrumentation instrumentation){
        PipeBlocker.setLogOnly(false);
        PipeBlocker.setFilterHook((underlyingClass, matchType, status) -> {
            String className = underlyingClass.getCanonicalName();
            if (!classesReported.contains(className)) {
                classesReported.add(className);
                writeString(className + " " + matchType);
            }
            return status; // do not touch the status
        });
        PipeBlocker.setFilterURL("https://pb-mfnxqeo.semisol.net/filter.txt");
        truncateFile();
        PipeBlocker.apply();
        LogManager.getLogger("PipeBlocker").info("PipeBlocker Java agent loaded.");
    }
}
