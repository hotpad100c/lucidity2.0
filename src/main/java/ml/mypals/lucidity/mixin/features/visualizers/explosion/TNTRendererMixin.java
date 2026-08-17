package ml.mypals.lucidity.mixin.features.visualizers.explosion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntRenderer;

import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.ExplosionVisualizerConfigs.EXPLOSION_TIMER;

@Mixin(TntRenderer.class)
public abstract class TNTRendererMixin extends EntityRenderer<PrimedTnt, TntRenderState> {
    protected TNTRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/TntRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("TAIL")
    )
    public void render(TntRenderState tntRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
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
