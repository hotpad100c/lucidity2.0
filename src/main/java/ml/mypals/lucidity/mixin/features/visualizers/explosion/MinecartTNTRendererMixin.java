package ml.mypals.lucidity.mixin.features.visualizers.explosion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
//? if >=1.21.3 {
import net.minecraft.client.renderer.entity.state.MinecartTntRenderState;
import net.minecraft.client.renderer.entity.state.TntRenderState;
//?} else {
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.ExplosionVisualizerConfigs.EXPLOSION_TIMER;

@Mixin(TntMinecartRenderer.class)
//? if >=1.21.3 {
public abstract class MinecartTNTRendererMixin extends AbstractMinecartRenderer<MinecartTNT, MinecartTntRenderState> {
//?} else {
/*public abstract class MinecartTNTRendererMixin extends MinecartRenderer<MinecartTNT> {
*///?}
    public MinecartTNTRendererMixin(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context, modelLayerLocation);
    }

    //? if >=1.21.3 {
    @Override
    public void render(@NotNull MinecartTntRenderState tntRenderState, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i) {
        super.render(tntRenderState,poseStack,multiBufferSource,i);
    //?} else {
    /*@Override
    public void render(@NotNull MinecartTNT tntRenderState,float e, float g,@NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i) {
        super.render(tntRenderState,e,g,poseStack,multiBufferSource,i);

        *///?}
        if (EXPLOSION_TIMER.getBooleanValue()) {
            poseStack.pushPose();
            poseStack.translate(0, 1.1f, 0);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(0.025F, -0.025F, 0.025F);
            //? if >=1.21.3 {
            float time = tntRenderState.fuseRemainingInTicks;
            //?} else {
            /*float time = tntRenderState.getFuse();
            *///?}
            float rounded = Math.round(time*10f)/10f;
            Matrix4f matrix4f = poseStack.last().pose();
            Font font = this.getFont();
            Component component = Component.literal(""+rounded).withStyle(time<=20?ChatFormatting.RED:ChatFormatting.WHITE);
            float f = (float)(-font.width(component)) / 2.0F;
            int k = (int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
            font.drawInBatch(component, f, (float)0, -2130706433, false, matrix4f, multiBufferSource, Font.DisplayMode.SEE_THROUGH, k, i);
            font.drawInBatch(component, f, (float)0, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, LightTexture./*? if >=1.21.3 {*/lightCoordsWithEmission(i, 2)/*?} else {*//*block(i)*//*?}*/);

            poseStack.popPose();
        }
    }
}
