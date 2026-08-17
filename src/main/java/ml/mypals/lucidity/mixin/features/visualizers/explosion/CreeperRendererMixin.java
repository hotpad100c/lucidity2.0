package ml.mypals.lucidity.mixin.features.visualizers.explosion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;

import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.TntRenderState;
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
public abstract class CreeperRendererMixin extends MobRenderer<Creeper, CreeperRenderState, CreeperModel> {

    public CreeperRendererMixin(EntityRendererProvider.Context context, CreeperModel entityModel, float f) {
        super(context, entityModel, f);
    }
    @Override
    public void submit(@NotNull CreeperRenderState creeperRenderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        super.submit(creeperRenderState, poseStack, submitNodeCollector, cameraRenderState);
        if (EXPLOSION_TIMER.getBooleanValue()) {
            poseStack.pushPose();


            poseStack.translate(0, creeperRenderState.eyeHeight + (double)0.5F, 0);

            poseStack.mulPose(cameraRenderState.orientation);
            poseStack.scale(0.025F, -0.025F, 0.025F);

            float time = 1 - creeperRenderState.swelling;
            float rounded = Math.round(time * 10f) / 10f;
            if(rounded < 0.99) {
                Matrix4f matrix4f = poseStack.last().pose();
                Font font = this.getFont();
                Component component = Component.literal("" + rounded).withStyle(rounded <= 0.5 ? ChatFormatting.RED : ChatFormatting.WHITE);
                float f = (float) (-font.width(component)) / 2.0F;
                int k = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;

                submitNodeCollector.submitNameTag(poseStack, new Vec3(0,creeperRenderState.eyeHeight+0.5,0), 0, component, !creeperRenderState.isDiscrete, creeperRenderState.lightCoords, creeperRenderState.distanceToCameraSq, cameraRenderState);
            }
            poseStack.popPose();
        }
    }
}
