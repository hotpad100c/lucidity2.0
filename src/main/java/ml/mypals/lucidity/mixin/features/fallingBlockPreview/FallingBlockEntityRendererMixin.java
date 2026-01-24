package ml.mypals.lucidity.mixin.features.fallingBlockPreview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.features.visualizers.b36Target.TransparentVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
//? if >=1.21.3 {
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.world.level.block.FallingBlock;
//?}
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.FeatureToggle.FALLING_BLOCK_PREVIEW;
import static ml.mypals.lucidity.features.fallingBlockPreview.FallingBlockPredictor.predictLandingPos;

@Mixin(FallingBlockRenderer.class)
public class FallingBlockEntityRendererMixin {
    @Shadow @Final private BlockRenderDispatcher dispatcher;

    //? if >=1.21.3 {
    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/FallingBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                    value = "TAIL"))
    public void render(FallingBlockRenderState fallingBlockRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "render(Lnet/minecraft/world/entity/item/FallingBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                    value = "TAIL"))
    public void render(FallingBlockEntity fallingBlockEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
    *///?}

        if(Minecraft.getInstance().level == null || !FALLING_BLOCK_PREVIEW.getBooleanValue()) return;
        //? if >=1.21.3 {
        BlockState blockState = fallingBlockRenderState.blockState;
        BlockPos predictLandingPos = predictLandingPos(
                Minecraft.getInstance().level,
                fallingBlockRenderState.x,
                fallingBlockRenderState.y,
                fallingBlockRenderState.z,
                blockState);
        //?} else {
        /*BlockState blockState = fallingBlockEntity.getBlockState();
        BlockPos predictLandingPos = predictLandingPos(
                Minecraft.getInstance().level,
                fallingBlockEntity.getX(),
                fallingBlockEntity.getY(),
                fallingBlockEntity.getZ(),
                blockState);
        *///?}
        if (predictLandingPos != null) {
            poseStack.pushPose();

            //? if >=1.21.3 {
            double offsetX = predictLandingPos.getX() - fallingBlockRenderState.x;
            double offsetY = predictLandingPos.getY() - fallingBlockRenderState.y;
            double offsetZ = predictLandingPos.getZ() - fallingBlockRenderState.z;
            //?} else {
            /*double offsetX = predictLandingPos.getX() - fallingBlockEntity.getX();
            double offsetY = predictLandingPos.getY() - fallingBlockEntity.getY();
            double offsetZ = predictLandingPos.getZ() - fallingBlockEntity.getZ();

            *///?}
            poseStack.translate(offsetX, offsetY, offsetZ);
            poseStack.scale(1.001f, 1.001f, 1.001f);

            VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.translucent());

            this.dispatcher.getModelRenderer().tesselateBlock(
                    Minecraft.getInstance().level,
                    //? if >=1.21.5 {
                    this.dispatcher.getBlockModel(blockState).collectParts(Minecraft.getInstance().level.getRandom()),
                    //?} else {
                    /*this.dispatcher.getBlockModel(blockState),
                    *///?}
                    blockState,
                    predictLandingPos,
                    poseStack,
                    new TransparentVertexConsumer(consumer),
                    true,
                    //? if <=1.21.4 {
                    /*RandomSource.create(),
                    blockState.getSeed(predictLandingPos),
                    *///?}
                    i);
            poseStack.popPose();
        }
    }
}
