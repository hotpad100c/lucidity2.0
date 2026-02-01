package ml.mypals.lucidity.mixin.features.worldEatwrMineHelper.fabric;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
//? if >=1.21.5 {
/*import net.minecraft.client.renderer.block.model.BlockStateModel;
*///?} else {
import net.minecraft.client.resources.model.BakedModel;
 //?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import static ml.mypals.lucidity.config.FeatureToggle.WORLD_EATER_MINE_HELPER;
import static ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager.getExtruded;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = TerrainRenderContext.class,remap = false)
public class TerrainRenderContextMixin {
    //? if >=1.21.5 {
    /*@WrapMethod(method = "bufferModel")
    public void tessellateBlock(BlockStateModel model, BlockState blockState, BlockPos blockPos, Operation<Void> original) {
        if (WORLD_EATER_MINE_HELPER.getBooleanValue() && WorldEaterHelperManager.shouldRender(blockState,blockPos)) {
            original.call(getExtruded(model), blockState, blockPos);
        }else{
            original.call(model, blockState, blockPos);
        }
    }

    *///?} else {
    @WrapMethod(method = "tessellateBlock")
    public void tessellateBlock(BlockState blockState, BlockPos blockPos, BakedModel model, PoseStack matrixStack, Operation<Void> original) {
        if (WORLD_EATER_MINE_HELPER.getBooleanValue() && WorldEaterHelperManager.shouldRender(blockState,blockPos)) {
            original.call(blockState, blockPos, getExtruded(model), matrixStack);
        }else{
            original.call(blockState, blockPos, model, matrixStack);
        }
    }
    //?}
}
