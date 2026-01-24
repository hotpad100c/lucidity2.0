package ml.mypals.lucidity.mixin.features.zalgoDeobf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;

import static ml.mypals.lucidity.config.FeatureToggle.ZALGO_TEXT_DEOBF;

@Mixin(Style.class)
public class TextRendererMixin {
    @WrapMethod(method = "isObfuscated")
    public boolean isObfuscated(Operation<Boolean> original) {
        return original.call() && !ZALGO_TEXT_DEOBF.getBooleanValue();
    }
}
