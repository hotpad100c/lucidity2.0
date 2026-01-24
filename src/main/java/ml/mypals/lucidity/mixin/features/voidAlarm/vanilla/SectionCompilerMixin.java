package ml.mypals.lucidity.mixin.features.voidAlarm.vanilla;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.texture.OverlayTexture;
//? if >=1.21.5 {
import net.minecraft.client.renderer.block.model.BlockStateModel;
//?} else {
/*import net.minecraft.client.resources.model.BakedModel;
 *///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import static ml.mypals.lucidity.config.FeatureToggle.VOID_ALARM;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {


    @Shadow protected abstract BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> map, SectionBufferBuilderPack sectionBufferBuilderPack, RenderType renderType);


    @Shadow @Final private BlockRenderDispatcher blockRenderer;

    @Inject(
            method = "compile",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                    shift = At.Shift.AFTER,
                    ordinal = 0
            )
    )
    private void renderBottomGlassAfterBlock(

            SectionPos sectionPos,
            RenderChunkRegion renderChunkRegion,
            VertexSorting vertexSorting,
            SectionBufferBuilderPack sectionBufferBuilderPack,
            CallbackInfoReturnable<SectionCompiler.Results> cir,
            @Local PoseStack poseStack,
            @Local Map<RenderType, BufferBuilder> map,
            @Local RandomSource randomSource,
            @Local(ordinal = 2) BlockPos blockPos3) {
        if(!VOID_ALARM.getBooleanValue()) return;


        //? if >=1.21.3 {
        int minWorldY = renderChunkRegion.getMinY();
        //?} else {
        /*int minWorldY = renderChunkRegion.getMinBuildHeight();
        *///?}


        BlockState blockState = renderChunkRegion.getBlockState(blockPos3);

        if (blockPos3.getY() == minWorldY && blockState.getRenderShape() != RenderShape.MODEL) {
            BlockState redGlass = Blocks.RED_STAINED_GLASS.defaultBlockState();
            RenderType renderType = RenderType.translucent();
            BufferBuilder bufferBuilder = this.getOrBeginLayer(map, sectionBufferBuilderPack, renderType);
            poseStack.pushPose();
            poseStack.translate((float)SectionPos.sectionRelative(blockPos3.getX()), (float)SectionPos.sectionRelative(blockPos3.getY()), (float)SectionPos.sectionRelative(blockPos3.getZ()));
            //? if >=1.21.5 {
            BlockStateModel model = this.blockRenderer.getBlockModel(redGlass);
            //?} else {
            /*BakedModel model = this.blockRenderer.getBlockModel(redGlass);
             *///?}
            tesselateVoidPlane(renderChunkRegion,
                    model,redGlass, blockPos3, poseStack,
                    bufferBuilder, true, randomSource,
                    blockState.getSeed(blockPos3), OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
    @Unique
    //? if >=1.21.5 {
    public void tesselateVoidPlane(BlockAndTintGetter blockAndTintGetter, BlockStateModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i) {

    //?} else {
    
    /*public void tesselateVoidPlane(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i) {
     *///?}

        BitSet bitSet = new BitSet(3);
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

        //? if >=1.21.5 {
        int j = LevelRenderer.getLightColor(blockAndTintGetter, mutableBlockPos);
        //this.blockRenderer.getModelRenderer().render(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, true,i,OverlayTexture.NO_OVERLAY);
        //TODO
        //?} else {
        /*List<BakedQuad> list = bakedModel.getQuads(blockState, Direction.UP, randomSource);
        if (!list.isEmpty()) {
            int j = LevelRenderer.getLightColor(blockAndTintGetter, blockState, mutableBlockPos);
            this.blockRenderer.getModelRenderer().renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, LightTexture.FULL_BLOCK, i, false, poseStack, vertexConsumer, list, bitSet);
        }
        *///?}
    }
}