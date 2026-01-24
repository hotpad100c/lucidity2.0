package ml.mypals.lucidity.mixin.features.yaclLikeConfig;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ButtonBase.class,remap = false)
public interface ButtonBaseAccessor {
    @Accessor
    String getDisplayString();
    @Accessor
    boolean getEnabled();
}
