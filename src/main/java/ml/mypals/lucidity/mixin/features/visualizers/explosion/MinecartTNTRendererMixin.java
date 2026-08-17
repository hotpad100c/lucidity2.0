package ml.mypals.lucidity.mixin.features.visualizers.explosion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.MinecartTntRenderState;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.ExplosionVisualizerConfigs.EXPLOSION_TIMER;

@Mixin(TntMinecartRenderer.class)
public abstract class MinecartTNTRendererMixin extends AbstractMinecartRenderer<MinecartTNT, MinecartTntRenderState> {
    public MinecartTNTRendererMixin(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context, modelLayerLocation);
    }

    @Override
    public void submit(@NotNull MinecartTntRenderState tntRenderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        super.submit(tntRenderState, poseStack, submitNodeCollector, cameraRenderState);
        if (EXPLOSION_TIMER.getBooleanValue()) {
            poseStack.pushPose();
            poseStack.translate(0, 1.1f, 0);

            poseStack.mulPose(cameraRenderState.orientation);

            poseStack.scale(0.025F, -0.025F, 0.025F);
            float time = tntRenderState.fuseRemainingInTicks;
            float rounded = Math.round(time*10f)/10f;
            Matrix4f matrix4f = poseStack.last().pose();
            Font font = this.getFont();
            Component component = Component.literal(""+rounded).withStyle(time<=20?ChatFormatting.RED:ChatFormatting.WHITE);
            float f = (float)(-font.width(component)) / 2.0F;
            int k = (int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
            submitNodeCollector.submitNameTag(poseStack, new Vec3(0,1.1,0), 0, component, !tntRenderState.isDiscrete, tntRenderState.lightCoords, tntRenderState.distanceToCameraSq, cameraRenderState);
            poseStack.popPose();
        }
    }
}
