package info.mmpa.pipeblocker;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class PipeBlockerInitializer implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        PipeBlocker.useLog4j();
        PipeBlocker.apply();
    }
}
