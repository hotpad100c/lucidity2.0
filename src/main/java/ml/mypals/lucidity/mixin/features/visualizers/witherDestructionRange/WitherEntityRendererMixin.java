package ml.mypals.lucidity.mixin.features.visualizers.witherDestructionRange;

//? if >=1.21.5 {
import com.mojang.blaze3d.opengl.GlStateManager;
//?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
*///?}
import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.malilib.util.data.Color4f;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
//? if >=1.21.3 {

import net.minecraft.client.renderer.entity.state.WitherRenderState;
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
    @Shadow protected abstract void scale(WitherRenderState witherRenderState, PoseStack poseStack);
    public WitherEntityRendererMixin(EntityRendererProvider.Context context, WitherBossModel entityModel, float f) {
        super(context, entityModel, f);
    }
    @Override
    public void render(@NotNull WitherRenderState witherRenderState, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i) {
        super.render(witherRenderState, poseStack, multiBufferSource, i);
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
            renderBox(poseStack,multiBufferSource.getBuffer(RenderType.debugQuads()), destructionBox,color.r,color.g,color.b,color.a);
            for(i = 0;i<3;i++){
                double x = getHeadX(witherRenderState,i+1);
                double y = getHeadY(witherRenderState,i+1);
                double z = getHeadZ(witherRenderState,i+1);

                GlStateManager._disableDepthTest();
                renderBox(poseStack,multiBufferSource.getBuffer(RenderType.debugQuads()),
                        new AABB(x+0.1,y+0.1,z+0.1,x-0.1,y-0.1,z-0.1)
                        ,color.r,color.g,color.b,color.a);

                GlStateManager._enableDepthTest();
            }
            poseStack.popPose();
        }
    }
    @Unique
    //? if >=1.21.3 {
    private double getHeadX(WitherRenderState witherRenderState, int i) {
        if (i <= 0) {
            return witherRenderState.x;
        } else {
            float f = (witherRenderState.bodyRot + (float)(180 * (i - 1))) * ((float)Math.PI / 180F);
            float g = Mth.cos(f);
            return witherRenderState.x + (double)g * 1.3 * (double)witherRenderState.scale;
        }
    }@Unique
    private double getHeadY(WitherRenderState witherRenderState, int i) {
        float f = i <= 0 ? 3.0F : 2.2F;
        return witherRenderState.y + (double)(f * witherRenderState.scale);
    }
    @Unique
    private double getHeadZ(WitherRenderState witherRenderState, int i) {
        if (i <= 0) {
            return witherRenderState.z;
        } else {
            float f = (witherRenderState.bodyRot + (float)(180 * (i - 1))) * ((float)Math.PI / 180F);
            float g = Mth.sin(f);
            return witherRenderState.z + (double)g * 1.3 * (double)witherRenderState.scale;
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
        double minY = offsetY;
        double minZ = offsetZ - j;
        double maxX = offsetX + j + 1;
        double maxY = offsetY + k + 1;
        double maxZ = offsetZ + j + 1;

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    //?} else {
    /*private double getHeadX(WitherBoss witherRenderState, int i) {
        if (i <= 0) {
            return witherRenderState.getX();
        } else {
            float f = (witherRenderState.yBodyRot + (float)(180 * (i - 1))) * ((float)Math.PI / 180F);
            float g = Mth.cos(f);
            return witherRenderState.getX() + (double)g * 1.3 * (double)witherRenderState.getScale();
        }
    }@Unique
    private double getHeadY(WitherBoss witherRenderState, int i) {
        float f = i <= 0 ? 3.0F : 2.2F;
        return witherRenderState.getY() + (double)(f * witherRenderState.getScale());
    }
    @Unique
    private double getHeadZ(WitherBoss witherRenderState, int i) {
        if (i <= 0) {
            return witherRenderState.getZ();
        } else {
            float f = (witherRenderState.yBodyRot + (float)(180 * (i - 1))) * ((float)Math.PI / 180F);
            float g = Mth.sin(f);
            return witherRenderState.getZ() + (double)g * 1.3 * (double)witherRenderState.getScale();
        }
    }
    @Unique
    private static @NotNull AABB getDestructionBox(@NotNull WitherBoss witherRenderState) {
        float bbw = witherRenderState.getBbWidth();
        float bbh = witherRenderState.getBbHeight();
        int j = Mth.floor(bbw / 2.0F + 1.0F);
        int k = Mth.floor(bbh);

        double worldX = witherRenderState.getX();
        double worldY = witherRenderState.getY();
        double worldZ = witherRenderState.getZ();

        int blockX = Mth.floor(worldX);
        int blockY = Mth.floor(worldY);
        int blockZ = Mth.floor(worldZ);

        double offsetX = blockX - worldX;
        double offsetY = blockY - worldY;
        double offsetZ = blockZ - worldZ;

        double minX = offsetX - j;
        double minY = offsetY;
        double minZ = offsetZ - j;
        double maxX = offsetX + j + 1;
        double maxY = offsetY + k + 1;
        double maxZ = offsetZ + j + 1;

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    *///?}
    }
