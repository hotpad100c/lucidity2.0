package ml.mypals.lucidity.mixin.features.worldEatwrMineHelper.fabric;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import static ml.mypals.lucidity.config.FeatureToggle.WORLD_EATER_MINE_HELPER;
import static ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager.getExtruded;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = TerrainRenderContext.class,remap = false)
public class TerrainRenderContextMixin {
    @WrapMethod(method = "bufferModel")
    public void tessellateBlock(BlockStateModel model, BlockState blockState, BlockPos blockPos, Operation<Void> original) {
        if (WORLD_EATER_MINE_HELPER.getBooleanValue() && WorldEaterHelperManager.shouldRender(blockState,blockPos)) {
            original.call(getExtruded(model), blockState, blockPos);
        }else{
            original.call(model, blockState, blockPos);
        }
    }

}
