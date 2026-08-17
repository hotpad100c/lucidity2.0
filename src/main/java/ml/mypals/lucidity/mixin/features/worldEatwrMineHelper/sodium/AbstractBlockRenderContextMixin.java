package ml.mypals.lucidity.mixin.features.worldEatwrMineHelper.sodium;

import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterTransformState;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = AbstractBlockRenderContext.class, remap = false)
public class AbstractBlockRenderContextMixin {

    @Inject(method = "isFaceCulled", at = @At("HEAD"), cancellable = true, remap = false)
    private void lucidity$dontCullExtrudedModel(Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (WorldEaterTransformState.isRenderingTransformed()) {
            cir.setReturnValue(false);
        }
    }
}
