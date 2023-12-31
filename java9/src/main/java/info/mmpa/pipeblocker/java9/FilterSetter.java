package info.mmpa.pipeblocker.java9;

import java.io.ObjectInputFilter;

import info.mmpa.pipeblocker.PipeBlocker;

public class FilterSetter {
    public static void apply () {
        ObjectInputFilter.Config.setSerialFilter(filterInfo -> {
            switch (PipeBlocker.check(filterInfo.serialClass())) {
                case UNDECIDED: return ObjectInputFilter.Status.UNDECIDED;
                case ALLOWED: return ObjectInputFilter.Status.ALLOWED;
                case REJECTED: return ObjectInputFilter.Status.REJECTED;
            }
            throw new AssertionError("unknown check status");
        });
    }
}
