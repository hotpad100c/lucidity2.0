package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;

import static ml.mypals.lucidity.config.FeatureToggle.FLUID_TRANSPARENCY_OVERRIDE;

@Mixin(ItemBlockRenderTypes.class)
public class RenderLayersMixin {
    @WrapMethod(method = "getRenderLayer")
    private static RenderType injectCustomFluidRenderLayer(FluidState fluidState, Operation<RenderType> original) {
        if (FLUID_TRANSPARENCY_OVERRIDE.getBooleanValue()) {
            return RenderType.translucent();
        }else{
            return original.call(fluidState);
        }
    }
}
