package ml.mypals.lucidity.mixin.features.visualizers.explosion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.CreeperModel;
//? if >=1.21.9 {
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
//?}
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
//? if >=1.21.3 {

import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.TntRenderState;
//?} else {
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.ExplosionVisualizerConfigs.EXPLOSION_TIMER;

@Mixin(CreeperRenderer.class)
public abstract class CreeperRendererMixin extends MobRenderer<Creeper,/*? if >=1.21.3 {*/CreeperRenderState,/*?}*/ CreeperModel/*? if <=1.21.1 {*//*<Creeper>*//*?}*/> {


    public CreeperRendererMixin(EntityRendererProvider.Context context, CreeperModel/*? if <=1.21.1 {*//*<Creeper>*//*?}*/ entityModel, float f) {
        super(context, entityModel, f);
    }
    //? if >=1.21.9 {
    @Override
    public void submit(@NotNull CreeperRenderState creeperRenderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        super.submit(creeperRenderState, poseStack, submitNodeCollector, cameraRenderState);
    //?} else if >=1.21.3 {
    /*@Override
    public void render(@NotNull CreeperRenderState creeperRenderState, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i) {
        super.render(creeperRenderState, poseStack, multiBufferSource, i);
    *///?} else {

    /*@Override
    public void render(@NotNull Creeper creeperRenderState, float e, float g, @NotNull PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        super.render(creeperRenderState,e,g, poseStack, multiBufferSource, i);
    *///?}
        if (EXPLOSION_TIMER.getBooleanValue()) {
            poseStack.pushPose();


            //? if >=1.21.3 {
            poseStack.translate(0, creeperRenderState.eyeHeight + (double)0.5F, 0);
            //?} else {
            /*poseStack.translate(0, creeperRenderState.getEyeHeight() + (double)0.5F, 0);
            *///?}

            //? if >=1.21.9 {
            poseStack.mulPose(cameraRenderState.orientation);
            //?} else {
            /*poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            *///?}
            poseStack.scale(0.025F, -0.025F, 0.025F);

            //? if >=1.21.3 {
            float time = 1 - creeperRenderState.swelling;
            //?} else {
            /*float time = 1 - creeperRenderState.swell;
            *///?}
            float rounded = Math.round(time * 10f) / 10f;
            if(rounded < 0.99) {
                Matrix4f matrix4f = poseStack.last().pose();
                Font font = this.getFont();
                Component component = Component.literal("" + rounded).withStyle(rounded <= 0.5 ? ChatFormatting.RED : ChatFormatting.WHITE);
                float f = (float) (-font.width(component)) / 2.0F;
                int k = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;

                //? if >=1.21.9 {
                submitNodeCollector.submitNameTag(poseStack, new Vec3(0,creeperRenderState.eyeHeight+0.5,0), 0, component, !creeperRenderState.isDiscrete, creeperRenderState.lightCoords, creeperRenderState.distanceToCameraSq, cameraRenderState);
                //?} else {
                /*font.drawInBatch(component, f, (float) 0, -2130706433, false, matrix4f, multiBufferSource, Font.DisplayMode.SEE_THROUGH, k, i);
                font.drawInBatch(component, f, (float) 0, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, LightTexture./^? if >=1.21.3 {^/lightCoordsWithEmission(i, 2)/^?} else {^//^block(i)^//^?}^/);
                *///?}
            }
            poseStack.popPose();
        }
    }
}
