package ml.mypals.lucidity.mixin.features.visualizers.witherDestructionRange;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.malilib.util.data.Color4f;
import ml.mypals.lucidity.utils.DeferredGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.monster.wither.WitherBossModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
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
public abstract class WitherEntityRendererMixin extends MobRenderer<WitherBoss, WitherRenderState, WitherBossModel> {
    @Shadow protected abstract void scale(@NotNull WitherRenderState witherRenderState, @NotNull PoseStack poseStack);
    public WitherEntityRendererMixin(EntityRendererProvider.Context context, WitherBossModel entityModel, float f) {
        super(context, entityModel, f);
    }
    @Override
    public void submit(@NotNull WitherRenderState witherRenderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        super.submit(witherRenderState, poseStack, submitNodeCollector, cameraRenderState);

        if (WITHER_DESTRUCTION_VISUALIZE.getBooleanValue()) {

            poseStack.pushPose();
            AABB destructionBox = getDestructionBox(witherRenderState);
            Color4f color = WITHER_DESTRUCTION_RANGE_COLOR.getColor();
            // 绘制必须走提交节点：submit 阶段直接写 bufferSource 的话这一帧不会画出来
            DeferredGeometry.submit(submitNodeCollector, poseStack, RenderTypes.debugQuads(),
                    (ps, consumer) -> renderBox(ps, consumer, destructionBox, color.r, color.g, color.b, color.a));

            poseStack.popPose();
        }
    }
    @Unique
    private static @NotNull AABB getDestructionBox(@NotNull WitherRenderState witherRenderState) {
        float bbw = witherRenderState.boundingBoxWidth;
        float bbh = witherRenderState.boundingBoxHeight;
        int j = Mth.floor(bbw / 2.0F + 1.0F);
        int k = Mth.floor(bbh);

        double worldX = witherRenderState.x;
        double worldY = witherRenderState.y;
        double worldZ = witherRenderState.z;

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
