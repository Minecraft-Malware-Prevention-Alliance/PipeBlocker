package info.mmpa.pipeblocker;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Mod(modid = "pipeblocker", name = "PipeBlocker", version = Tags.VERSION, acceptableRemoteVersions = "*")
public class PipeBlocker {

    public static final Logger LOGGER = LogManager.getLogger("PipeBlocker");

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void init(FMLInitializationEvent ev) {
        if (Objects.equals(System.getProperty("info.mmpa.pipeblocker.log-only"), "true")) {
            LOGGER.fatal("**************************************************************");
            LOGGER.fatal("*  WARNING: You are running PipeBlocker with log only mode.  *");
            LOGGER.fatal("*                                                            *");
            LOGGER.fatal("* This means the protections of PipeBlocker are disabled and *");
            LOGGER.fatal("* would be blocked deserialization attempts are only logged. *");
            LOGGER.fatal("* While this useful for figuring out what is broken, you     *");
            LOGGER.fatal("* SHOULD NOT use this mode while playing on multiplayer      *");
            LOGGER.fatal("* servers.                                                   *");
            LOGGER.fatal("*                                                            *");
            LOGGER.fatal("* You should disable this unless you are trying to figure    *");
            LOGGER.fatal("* out what mod is causing issues using the instructions on   *");
            LOGGER.fatal("* GitHub, or you were asked to on our issue tracker.         *");
            LOGGER.fatal("**************************************************************");
        } else {
            LOGGER.info("Pipes being blocked.");
        }
    }
}
