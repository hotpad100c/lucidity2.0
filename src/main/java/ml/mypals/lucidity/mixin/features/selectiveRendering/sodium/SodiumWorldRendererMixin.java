package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentBuffersWrapper;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentVertexConsumer;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.SortedSet;

@Mixin(value = SodiumWorldRenderer.class,remap = false)
public class SodiumWorldRendererMixin {
    //? if <1.21.9 {
    @WrapMethod(method = "renderBlockEntity")
    private static void renderBlockEntity(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher, BlockEntity entity, LocalPlayer player, LocalBooleanRef isGlowing, Operation<Void> original) {
        BlockPos pos = entity.getBlockPos();
        BlockState state = entity.getBlockState();
        if(!SelectiveRenderingManager.shouldRenderBlock(state,pos)
                && !SelectiveRenderingConfigs.isBlockFullyHidden()){
            original.call(matrices, bufferBuilders, blockBreakingProgressions, tickDelta, new ControllableTransparentBuffersWrapper(immediate), x, y, z, dispatcher, entity, player, isGlowing);
        }else {
            original.call(matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z, dispatcher, entity, player, isGlowing);
        }
    }
    //?}
}
