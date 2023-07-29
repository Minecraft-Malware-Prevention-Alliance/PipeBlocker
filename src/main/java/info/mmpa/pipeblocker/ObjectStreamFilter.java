package info.mmpa.pipeblocker;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.misc.ObjectInputFilter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ObjectStreamFilter {
    private static final Logger LOGGER = LogManager.getLogger("PipeBlocker");

    private static final ImmutableList<Class<?>> VALID_CLASSES = ImmutableList.of();

    private static final Set<Class<?>> REJECTED_CLASSES = Collections.synchronizedSet(new HashSet<>());

    public static void apply() {
        ObjectInputFilter.Config.setSerialFilter(new ObjectInputFilter() {
            @Override
            public Status checkInput(FilterInfo filterInfo) {
                if(filterInfo.serialClass() == null)
                    return Status.UNDECIDED;

                // TODO avoid horrible time complexity with large validation list
                for(Class<?> validParentClass : VALID_CLASSES) {
                    if(validParentClass.isAssignableFrom(filterInfo.serialClass())) {
                        return Status.UNDECIDED;
                    }
                }

                if(REJECTED_CLASSES.add(filterInfo.serialClass())) {
                    LOGGER.warn("Blocked class {} from being deserialized as it's not allowed", filterInfo.serialClass().getName());
                }
                return Status.REJECTED;
            }
        });
    }
}
