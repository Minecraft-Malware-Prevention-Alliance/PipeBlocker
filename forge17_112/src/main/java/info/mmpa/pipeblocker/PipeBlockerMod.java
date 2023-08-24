package info.mmpa.pipeblocker;

import info.mmpa.pipeblocker.logger.PipeBlockerLogger;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "pipeblocker", name = "PipeBlocker", version = Tags.VERSION, acceptableRemoteVersions = "*")
public class PipeBlockerMod {

    public static final PipeBlockerLogger LOGGER = PipeBlockerLogger.detectLogger();

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void init(FMLInitializationEvent ev) {
        PipeBlocker.chooseBestLogger();
    }
}
