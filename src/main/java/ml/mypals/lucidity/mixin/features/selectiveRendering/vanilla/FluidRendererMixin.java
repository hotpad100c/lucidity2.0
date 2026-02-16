package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import static ml.mypals.lucidity.config.LucidityConfigs.Generic.FLUID_TRANSPARENCY;
import static ml.mypals.lucidity.config.FeatureToggle.FLUID_TRANSPARENCY_OVERRIDE;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(LiquidBlockRenderer.class)
public class FluidRendererMixin {

    @WrapOperation(
            method = "tesselate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;vertex(Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFFFFFI)V"
            )
    )
    private void wrapVertex(LiquidBlockRenderer instance, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, int n, Operation<Void> original) {
        if(!FLUID_TRANSPARENCY_OVERRIDE.getBooleanValue()){
            original.call(instance, vertexConsumer, f, g, h, i, j, k, l, m, n);
        }else{
            vertexConsumer.addVertex(f, g, h)
                    .setColor(i, j, k, FLUID_TRANSPARENCY.getFloatValue())
                    .setUv(l, m)
                    .setLight(n)
                    .setNormal(0.0F, 1.0F, 0.0F);
        }
    }

    //? if >= 1.21.3 {
    
    /*@WrapOperation(
            method = "tesselate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)Z"
            )
    )
    private boolean redirectShouldRenderFace(
            FluidState fluidState,
            BlockState state,
            Direction side,
            FluidState fluidState2,
            Operation<Boolean> original,
            @Local(argsOnly = true) BlockPos pos
    ) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return original.call(fluidState, state, side, fluidState2);
        BlockState neighborState = world.getBlockState(pos.relative(side));
        boolean currentSpecial = shouldRenderBlock(state, pos);
        boolean neighborSpecial = shouldRenderBlock(neighborState, pos.relative(side));
        if (currentSpecial && !neighborSpecial) {
            return true;
        }
        if (!currentSpecial && neighborSpecial) {
            return false;
        }
        return original.call(fluidState, state, side, fluidState2);
    }

    @WrapOperation(
            method = "tesselate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;isFaceOccludedByNeighbor(Lnet/minecraft/core/Direction;FLnet/minecraft/world/level/block/state/BlockState;)Z"
            )
    )
    private boolean redirectIsFaceOccluded(
            Direction side,
            float f,
            BlockState neighborState,
            Operation<Boolean> original,
            @Local(argsOnly = true) BlockPos pos
    ) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return original.call(side, f, neighborState);
        BlockState currentState = world.getBlockState(pos);
        boolean currentSpecial = shouldRenderBlock(currentState, pos);
        boolean neighborSpecial = shouldRenderBlock(neighborState, pos.relative(side));
        if (currentSpecial && !neighborSpecial) {
            return false;
        }
        if (!currentSpecial && neighborSpecial) {
            return true;
        }
        return original.call(side, f, neighborState);
    }
    *///?} else {
    @WrapOperation(
            method = "tesselate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;shouldRenderFace(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)Z"
            )
    )
    private boolean redirectShouldRenderFace(
            BlockAndTintGetter blockAndTintGetter,
            BlockPos pos, FluidState fluidState,
            BlockState state, Direction side,
            FluidState fluidState2, Operation<Boolean> original
    ) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return original.call(blockAndTintGetter, pos, fluidState, state, side, fluidState2);
        BlockState neighborState = world.getBlockState(pos.relative(side));
        boolean currentSpecial = shouldRenderBlock(state, pos);
        boolean neighborSpecial = shouldRenderBlock(neighborState, pos.relative(side));
        if (currentSpecial && !neighborSpecial) {
            return true;
        }
        if (!currentSpecial && neighborSpecial) {
            return false;
        }
        return original.call(blockAndTintGetter, pos, fluidState, state, side, fluidState2);
    }

    @WrapMethod(
            method = "isFaceOccludedByState(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/Direction;FLnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"
    )
    private static boolean redirectIsFaceOccluded(
            BlockGetter blockGetter,
            Direction side,
            float f, BlockPos pos,
            BlockState neighborState,
            Operation<Boolean> original
    ) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return original.call(blockGetter, side, f, pos, neighborState);
        BlockState currentState = world.getBlockState(pos);
        boolean currentSpecial = shouldRenderBlock(currentState, pos);
        boolean neighborSpecial = shouldRenderBlock(neighborState, pos.relative(side));
        if (currentSpecial && !neighborSpecial) {
            return false;
        }
        if (!currentSpecial && neighborSpecial) {
            return true;
        }
        return original.call(blockGetter, side, f, pos, neighborState);
    }
    //?}
}
