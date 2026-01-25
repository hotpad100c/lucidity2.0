package ml.mypals.lucidity.mixin.features.visualizers.b36Target;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.features.visualizers.b36Target.TransparentVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
//? if >=1.21.5 {
import net.minecraft.client.renderer.block.model.BlockStateModel;
//?} else {
/*import net.minecraft.client.resources.model.BakedModel;
 *///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.FeatureToggle.B36_TARGET_PREVIEW;

@Mixin(PistonHeadRenderer.class)
public abstract class MovingBlockRenderMixin {
    @Unique
    private boolean renderAdditional = false;
    @Shadow protected abstract void renderBlock(BlockPos blockPos, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, Level level, boolean bl, int i);

    @Shadow @Final private BlockRenderDispatcher blockRenderer;

    //? if >=1.21.5 {
    @Inject(
            method = "render(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/phys/Vec3;)V",
            at = @At("TAIL")
    )private void renderPreviewBlock(
            PistonMovingBlockEntity piston, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Vec3 vec3, CallbackInfo ci
    )
    //?} else {
    /*@Inject(
            method = "render(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("TAIL")
    )private void renderPreviewBlock(
            PistonMovingBlockEntity piston, float f,
            PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci
    )
    *///?}
     {

        if(!B36_TARGET_PREVIEW.getBooleanValue()) return;

        BlockPos blockPos = piston.getBlockPos()
                .relative(piston.getMovementDirection().getOpposite());
        Direction dir = piston.getMovementDirection();
        BlockPos targetPos = blockPos.relative(dir);
        poseStack.pushPose();

        poseStack.translate(
                targetPos.getX() - piston.getBlockPos().getX(),
                targetPos.getY() - piston.getBlockPos().getY(),
                targetPos.getZ() - piston.getBlockPos().getZ()
        );

        renderAdditional = true;
        this.renderBlock(
                targetPos,
                piston.getMovedState(),
                poseStack,
                multiBufferSource,
                piston.getLevel(),
                false,
                j
        );

        poseStack.popPose();
    }
    @WrapMethod(method = "renderBlock")
    private void renderBlock(BlockPos blockPos, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, Level level, boolean bl, int i, Operation<Void> original) {
        if(renderAdditional){
            renderAdditional = false;
            //? if >=1.21.6 {
            /*VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.translucentMovingBlock());
            *///?} else {
            VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.translucent());
             //?}
            poseStack.scale(1.001f,1.001f,1.001f);
            //? if >=1.21.5 {
            assert Minecraft.getInstance().level != null;
            this.blockRenderer.getModelRenderer().tesselateBlock(level, this.blockRenderer.getBlockModel(blockState).collectParts(Minecraft.getInstance().level.getRandom()), blockState, blockPos, poseStack, new TransparentVertexConsumer(consumer) , true, i);

            //?} else {
            /*this.blockRenderer.getModelRenderer().tesselateBlock(level, this.blockRenderer.getBlockModel(blockState), blockState, blockPos, poseStack, new TransparentVertexConsumer(consumer) , true, Minecraft.getInstance().level.getRandom(), blockState.getSeed(blockPos), i);
            *///?}
        }else {
            original.call(blockPos, blockState, poseStack, multiBufferSource, level, bl, i);
        }
    }


}
