package info.mmpa.pipeblocker;

import info.mmpa.pipeblocker.ObjectStreamFilter;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class PipeBlockerInitializer implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        ObjectStreamFilter.apply();
    }
}
