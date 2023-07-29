package cpw.mods.fml.relauncher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * dummy class to keep compat with old fml
 *
 * if it's 1.7.10, forge's class will take precedence
 * if it's later, having this class will change nothing
 *
 * credits to cpw for the original class signature
 */
public interface IFMLLoadingPlugin {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Name {
        String value();
    }
}