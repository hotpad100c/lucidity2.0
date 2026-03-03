package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.chunk.VisGraph;
//? if >=1.21.5 {
import net.minecraft.client.renderer.block.model.BlockStateModel;
//?} else {
/*import net.minecraft.client.resources.model.BakedModel;
 *///?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Pseudo
@Mixin(value = ChunkBuilderMeshingTask.class,remap = false)
public abstract class ChunkBuilderMeshingTaskMixin{

    @WrapOperation(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/VisGraph;setOpaque(Lnet/minecraft/core/BlockPos;)V"
            ),
            remap = false
    )
    public void filterBlockRender(VisGraph instance, BlockPos blockPos, Operation<Void> original,@Local BlockState blockState){
        if(shouldRenderBlock(blockState,blockPos)){
            original.call(instance, blockPos);
        }
    }
    //? if >=1.21.5 {
    @WrapOperation(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lnet/minecraft/client/renderer/block/model/BlockStateModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V"
            ),
            remap = false
    )
    public void filterBlockRender(BlockRenderer instance, BlockStateModel type, BlockState blockState, BlockPos model, BlockPos state, Operation<Void> original){
        if(shouldRenderBlock(blockState,model) || !SelectiveRenderingConfigs.isBlockFullyHidden()){
            original.call(instance, type, blockState, model, state);
        }
    }
    //?} else {
    /*@WrapOperation(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V"
            ),
            remap = false
    )
    public void filterBlockRender(BlockRenderer instance, BakedModel type, BlockState blockState, BlockPos model, BlockPos state, Operation<Void> original){
        if(shouldRenderBlock(blockState,model) || !SelectiveRenderingConfigs.isBlockFullyHidden()){
            original.call(instance, type, blockState, model, state);
        }
    }
    *///?}

    @WrapOperation(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;" +
                    "Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;render(Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/TranslucentGeometryCollector;Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V"),
            remap = false
    )
    public void filterFluidRender(FluidRenderer instance, LevelSlice levelSlice, BlockState blockState,
                                  FluidState fluidState, BlockPos blockPos, BlockPos modelOffset,
                                  TranslucentGeometryCollector translucentGeometryCollector, ChunkBuildBuffers chunkBuildBuffers, Operation<Void> original){
        if(shouldRenderBlock(blockState,blockPos) || !SelectiveRenderingConfigs.isBlockFullyHidden()){
            original.call(instance, levelSlice, blockState, fluidState, blockPos,modelOffset,translucentGeometryCollector,chunkBuildBuffers);
        }
    }
    @WrapOperation(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;" +
                    "Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/data/BuiltSectionInfo$Builder;addBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Z)V"
            ),
            remap = false
    )
    public void filterBlockStateRender(BuiltSectionInfo.Builder builder, BlockEntity blockEntity, boolean b , Operation<Void> original, @Local BlockState blockState){
        if(shouldRenderBlock(blockState,blockEntity.getBlockPos()) || !SelectiveRenderingConfigs.isBlockFullyHidden()){
            original.call(builder,blockEntity,b);
        }
    }
}