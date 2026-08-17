package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;

import static ml.mypals.lucidity.config.FeatureToggle.FLUID_TRANSPARENCY_OVERRIDE;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.SelectiveRenderingMode.OFF;

@Mixin(ItemBlockRenderTypes.class)
public class RenderLayersMixin {
    @WrapMethod(method = "getRenderLayer")
    private static ChunkSectionLayer injectCustomFluidRenderLayer(FluidState fluidState, Operation<ChunkSectionLayer> original) {
        if (FLUID_TRANSPARENCY_OVERRIDE.getBooleanValue() ||
                SelectiveRenderingConfigs.BLOCK_RENDERING_MODE.getOptionListValue() != OFF) {
            return ChunkSectionLayer.TRANSLUCENT;
        }else{
            return original.call(fluidState);
        }
    }
}
