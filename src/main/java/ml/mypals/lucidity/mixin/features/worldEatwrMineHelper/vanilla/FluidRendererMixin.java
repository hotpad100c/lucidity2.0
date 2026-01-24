package ml.mypals.lucidity.mixin.features.worldEatwrMineHelper.vanilla;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ml.mypals.lucidity.features.worldEaterHelper.TransformedVertexConsumer;
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static ml.mypals.lucidity.config.LucidityConfigs.Generic.WORLD_EATER_MINE_HELPER_HEIGHT;
import static ml.mypals.lucidity.config.FeatureToggle.WORLD_EATER_MINE_HELPER;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(LiquidBlockRenderer.class)
public abstract class FluidRendererMixin {

    @Unique
    private static ThreadLocal<Boolean> isRenderingTransformed = ThreadLocal.withInitial(() -> false);

    @WrapOperation(method = "tesselate",at = @At(target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
    value = "INVOKE"))
    public int tesselate(LiquidBlockRenderer instance, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, Operation<Integer> original){

        return isRenderingTransformed.get() ? LightTexture.FULL_BRIGHT:original.call(instance,blockAndTintGetter,blockPos);
    }
    @WrapMethod(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V")
    public void tesselateAdditional(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, Operation<Void> original) {

        if (isRenderingTransformed.get()) {
            original.call(blockAndTintGetter, blockPos, vertexConsumer, blockState, fluidState);
            return;
        }

        if (WORLD_EATER_MINE_HELPER.getBooleanValue() && WorldEaterHelperManager.shouldRender(blockState, blockPos)) {
            isRenderingTransformed.set(true);
            try {
                PoseStack dummyPoseStack = new PoseStack();
                dummyPoseStack.pushPose();
                tesselateWithTransform(blockAndTintGetter, blockPos, dummyPoseStack, vertexConsumer, blockState, fluidState, WORLD_EATER_MINE_HELPER_HEIGHT.getFloatValue(), original);
                dummyPoseStack.popPose();
            } finally {
                isRenderingTransformed.set(false);
            }
        }
        original.call(blockAndTintGetter, blockPos, vertexConsumer, blockState, fluidState);
    }

    @Unique
    public void tesselateWithTransform(
            BlockAndTintGetter blockAndTintGetter,
            BlockPos blockPos,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            BlockState blockState,
            FluidState fluidState,
            float height,
            Operation<Void> original) {

        float relX = (float)(blockPos.getX() & 15);
        float relY = (float)(blockPos.getY() & 15);
        float relZ = (float)(blockPos.getZ() & 15);

        poseStack.translate(relX + 0.5, relY + 0.5 + height, relZ + 0.5);

        Quaternionf rotation = new Quaternionf().rotationTo(
                new Vector3f(1, 1, 1).normalize(),
                new Vector3f(0, 1, 0)
        );
        poseStack.mulPose(rotation);
        poseStack.mulPose(Axis.YP.rotationDegrees(45f));

        poseStack.translate(-(relX + 0.5), -(relY + 0.5), -(relZ + 0.5));

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer transformedConsumer = new TransformedVertexConsumer(vertexConsumer, matrix);

        original.call(blockAndTintGetter, blockPos, transformedConsumer, blockState, fluidState);
    }
    //? if >=1.21.3 {
    @WrapMethod(method = "shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)Z")
    private static boolean redirectShouldRenderFace(
            FluidState fluidState,
            BlockState state,
            Direction side,
            FluidState fluidState2,
            Operation<Boolean> original
    ) {
        return isRenderingTransformed.get() || original.call(fluidState, state, side, fluidState2);
    }

    @WrapMethod(method = "isFaceOccludedByNeighbor(Lnet/minecraft/core/Direction;FLnet/minecraft/world/level/block/state/BlockState;)Z")
    private static boolean redirectIsFaceOccluded(
            Direction side,
            float f,
            BlockState neighborState,
            Operation<Boolean> original
    ) {
        return !isRenderingTransformed.get() && original.call(side, f, neighborState);
    }
    //?} else {
    /*@WrapMethod(method = "shouldRenderFace(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)Z")
    private static boolean redirectShouldRenderFace(
            BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, FluidState fluidState, BlockState blockState, Direction direction, FluidState fluidState2, Operation<Boolean> original
    ) {
        return isRenderingTransformed.get() || original.call(blockAndTintGetter, blockPos, fluidState, blockState, direction, fluidState2);
    }

    @WrapMethod(method = "isFaceOccludedByState(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/Direction;FLnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z")
    private static boolean redirectIsFaceOccluded(
            BlockGetter blockGetter, Direction direction, float f, BlockPos blockPos, BlockState blockState, Operation<Boolean> original
    ) {
        return !isRenderingTransformed.get() && original.call(blockGetter, direction, f, blockPos, blockState);
    }
    *///?}
}
