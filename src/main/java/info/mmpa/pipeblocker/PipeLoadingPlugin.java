package info.mmpa.pipeblocker;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

// we support multiple minecraft version, so no @MCVersion
@IFMLLoadingPlugin.Name("PipeBlocker")
// naming clash, so fully qualified name
@cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name("PipeBlocker")
public class PipeLoadingPlugin implements IFMLLoadingPlugin, cpw.mods.fml.relauncher.IFMLLoadingPlugin {

    public PipeLoadingPlugin() {
        ObjectStreamFilter.apply();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "info.mmpa.pipeblocker.PipeTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
