package info.mmpa.pipeblocker;

import net.minecraft.launchwrapper.IClassTransformer;

public class PipeTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return basicClass;
    }
}
