package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
//? if >=1.21.6 {
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
//?}
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;

import static ml.mypals.lucidity.config.FeatureToggle.FLUID_TRANSPARENCY_OVERRIDE;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.SelectiveRenderingMode.OFF;

@Mixin(ItemBlockRenderTypes.class)
public class RenderLayersMixin {
    @WrapMethod(method = "getRenderLayer")
    //? if >=1.21.6 {
    private static ChunkSectionLayer injectCustomFluidRenderLayer(FluidState fluidState, Operation<ChunkSectionLayer> original) {
    //?} else {
    /*private static RenderType injectCustomFluidRenderLayer(FluidState fluidState, Operation<RenderType> original) {
    *///?}
        if (FLUID_TRANSPARENCY_OVERRIDE.getBooleanValue() ||
                !(SelectiveRenderingConfigs.isBlockFullyHidden() && SelectiveRenderingConfigs.BLOCK_RENDERING_MODE.getOptionListValue() != OFF)) {
            //? if >=1.21.6 {
            return ChunkSectionLayer.TRANSLUCENT;
            //?} else {
            /*return RenderType.translucent();
            *///?}
        }else{
            return original.call(fluidState);
        }
    }
}
