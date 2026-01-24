package ml.mypals.lucidity.mixin.features.worldEatwrMineHelper.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
//? if >=1.21.5 {
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
//?} else {
/*import net.minecraft.client.resources.model.BakedModel;
 *///?}
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static ml.mypals.lucidity.config.LucidityConfigs.Generic.WORLD_EATER_MINE_HELPER_HEIGHT;
import static ml.mypals.lucidity.config.FeatureToggle.WORLD_EATER_MINE_HELPER;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {

    //? if >=1.21.5 {
    @Shadow public abstract void tesselateWithAO(BlockAndTintGetter par1, List<BlockModelPart> par2, BlockState par3, BlockPos par4, PoseStack par5, VertexConsumer par6, boolean par7, int par8);

    @Shadow public abstract void tesselateWithoutAO(BlockAndTintGetter par1, List<BlockModelPart> par2, BlockState par3, BlockPos par4, PoseStack par5, VertexConsumer par6, boolean par7, int par8);

    //?} else {
    /*@Shadow public abstract void tesselateWithAO(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i);

    @Shadow public abstract void tesselateWithoutAO(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i);
    *///?}
    @Inject(method = "tesselateBlock", at = @At(value = "TAIL"))
    //? if >=1.21.5 {
    public void tesselateBlock(
            BlockAndTintGetter blockAndTintGetter, List<BlockModelPart> bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, int i, CallbackInfo ci) {

    //?} else {

    /*public void tesselateBlock(
            BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i, CallbackInfo ci) {
    *///?}
        if (WORLD_EATER_MINE_HELPER.getBooleanValue() && WorldEaterHelperManager.shouldRender(blockState,blockPos)) {
            float height = WORLD_EATER_MINE_HELPER_HEIGHT.getFloatValue();
            poseStack.pushPose();

            poseStack.translate(0.5, height+0.5, 0.5);
            Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(1, 1, 1).normalize(), new Vector3f(0, 1, 0));
            poseStack.mulPose(rotation);
            poseStack.mulPose(Axis.YP.rotationDegrees(45f));

            poseStack.translate(-0.5, -0.5, -0.5);

            //? if >=1.21.5 {
            boolean bl2 = Minecraft.useAmbientOcclusion() && blockState.getLightEmission() == 0 && bakedModel.getFirst().useAmbientOcclusion();
            //?} else {
            /*boolean bl2 = Minecraft.useAmbientOcclusion() && blockState.getLightEmission() == 0 && bakedModel.useAmbientOcclusion();
            *///?}
            try {
                //? if >=1.21.5 {
                if (bl2) {
                    this.tesselateWithAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, i);
                } else {
                    this.tesselateWithoutAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl,  i);
                }
                //?} else {
                /*if (bl2) {
                    this.tesselateWithAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, randomSource, LightTexture.FULL_BLOCK, i);
                } else {
                    this.tesselateWithoutAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, randomSource, LightTexture.FULL_BLOCK, i);
                }
                *///?}
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Tesselating block model");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Block model being tesselated");
                CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, blockState);
                crashReportCategory.setDetail("Using AO", bl2);
                throw new ReportedException(crashReport);
            }

            poseStack.popPose();

        }
    }
}
