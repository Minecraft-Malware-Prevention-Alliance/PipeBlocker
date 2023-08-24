package info.mmpa.pipeblocker.java8;

import info.mmpa.pipeblocker.PipeBlocker;
import sun.misc.ObjectInputFilter;

public class FilterSetter {
    private static boolean applied = false;
    public static void apply() {
        if (!applied) {
            ObjectInputFilter.Config.setSerialFilter(filterInfo -> {
                switch (PipeBlocker.check(filterInfo.serialClass())) {
                case UNDECIDED: return ObjectInputFilter.Status.UNDECIDED;
                case ALLOWED: return ObjectInputFilter.Status.ALLOWED;
                case REJECTED: return ObjectInputFilter.Status.REJECTED;
                }
                throw new AssertionError("unknown check status");
            });
            applied = true;
        }
    }
}
