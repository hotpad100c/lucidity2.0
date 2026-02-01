package ml.mypals.lucidity.mixin.features.visualizers.witherDestructionRange;

//? if >=1.21.5 {
/*import com.mojang.blaze3d.opengl.GlStateManager;
*///?} else {
import com.mojang.blaze3d.platform.GlStateManager;
//?}
import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
//? if >=1.21.9 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
*///?}
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
//? if >=1.21.3 {
import net.minecraft.client.renderer.entity.state.WitherRenderState;
//?} else {
//?}
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static ml.mypals.lucidity.config.VisualizerColors.WITHER_DESTRUCTION_RANGE_COLOR;
import static ml.mypals.lucidity.config.FeatureToggle.WITHER_DESTRUCTION_VISUALIZE;
import static ml.mypals.lucidity.utils.LucidityRenderUtils.renderBox;

@Mixin(WitherBossRenderer.class)
//? if >=1.21.3 {
public abstract class WitherEntityRendererMixin extends MobRenderer<WitherBoss, WitherRenderState, WitherBossModel> {
    @Shadow protected abstract void scale(@NotNull WitherRenderState witherRenderState, @NotNull PoseStack poseStack);
    public WitherEntityRendererMixin(EntityRendererProvider.Context context, WitherBossModel entityModel, float f) {
        super(context, entityModel, f);
    }
    //? if >=1.21.9 {
    /*@Override
    public void submit(@NotNull WitherRenderState witherRenderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        super.submit(witherRenderState, poseStack, submitNodeCollector, cameraRenderState);
    *///?} else {
    @Override
    public void render(@NotNull WitherRenderState witherRenderState, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i) {
        super.render(witherRenderState, poseStack, multiBufferSource, i);
    //?}
//?} else {
/*public abstract class WitherEntityRendererMixin extends MobRenderer<WitherBoss, WitherBossModel<WitherBoss>> {
    public WitherEntityRendererMixin(EntityRendererProvider.Context context, WitherBossModel<WitherBoss> entityModel, float f) {
        super(context, entityModel, f);
    }
    @Override
    public void render(@NotNull WitherBoss witherRenderState,float e,float g, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i) {
        super.render(witherRenderState,e,g, poseStack, multiBufferSource, i);
*///?}

        if (WITHER_DESTRUCTION_VISUALIZE.getBooleanValue()) {

            poseStack.pushPose();
            AABB destructionBox = getDestructionBox(witherRenderState);
            Color4f color = WITHER_DESTRUCTION_RANGE_COLOR.getColor();
            //? if >=1.21.9 {
            /*MultiBufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            *///?}
            renderBox(poseStack,multiBufferSource.getBuffer(RenderType.debugQuads()), destructionBox,color.r,color.g,color.b,color.a);

            poseStack.popPose();
        }
    }
    //? if >=1.21.3 {
    @Unique
    private static @NotNull AABB getDestructionBox(@NotNull WitherRenderState witherRenderState) {
        float bbw = witherRenderState.boundingBoxWidth;
        float bbh = witherRenderState.boundingBoxHeight;
        int j = Mth.floor(bbw / 2.0F + 1.0F);
        int k = Mth.floor(bbh);

        double worldX = witherRenderState.x;
        double worldY = witherRenderState.y;
        double worldZ = witherRenderState.z;
    //?} else {
    /*@Unique
    private static @NotNull AABB getDestructionBox(@NotNull WitherBoss witherRenderState) {
        float bbw = witherRenderState.getBbWidth();
        float bbh = witherRenderState.getBbHeight();
        int j = Mth.floor(bbw / 2.0F + 1.0F);
        int k = Mth.floor(bbh);

        double worldX = witherRenderState.getX();
        double worldY = witherRenderState.getY();
        double worldZ = witherRenderState.getZ();
    *///?}

        int blockX = Mth.floor(worldX);
        int blockY = Mth.floor(worldY);
        int blockZ = Mth.floor(worldZ);

        double offsetX = blockX - worldX;
        double offsetY = blockY - worldY;
        double offsetZ = blockZ - worldZ;

        double minX = offsetX - j;
        double minZ = offsetZ - j;
        double maxX = offsetX + j + 1;
        double maxY = offsetY + k + 1;
        double maxZ = offsetZ + j + 1;

        return new AABB(minX, offsetY, minZ, maxX, maxY, maxZ);
    }
}
