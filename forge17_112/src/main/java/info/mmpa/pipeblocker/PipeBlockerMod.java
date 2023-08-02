package info.mmpa.pipeblocker;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Mod(modid = "pipeblocker", name = "PipeBlocker", version = Tags.VERSION, acceptableRemoteVersions = "*")
public class PipeBlockerMod {

    public static final Logger LOGGER = LogManager.getLogger("PipeBlocker");

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void init(FMLInitializationEvent ev) {
    }
}
