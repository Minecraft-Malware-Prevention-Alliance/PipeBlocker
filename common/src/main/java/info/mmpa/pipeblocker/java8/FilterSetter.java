package info.mmpa.pipeblocker.java8;

import info.mmpa.pipeblocker.PipeBlocker;
import sun.misc.ObjectInputFilter;

public class FilterSetter {
    public static void apply() {
        ObjectInputFilter.Config.setSerialFilter(filterInfo -> {
            switch (PipeBlocker.check(filterInfo.serialClass())) {
                case UNDECIDED: return sun.misc.ObjectInputFilter.Status.UNDECIDED;
                case ALLOWED: return sun.misc.ObjectInputFilter.Status.ALLOWED;
                case REJECTED: return sun.misc.ObjectInputFilter.Status.REJECTED;
            }
            throw new AssertionError("unknown check status");
        });
    }
}
