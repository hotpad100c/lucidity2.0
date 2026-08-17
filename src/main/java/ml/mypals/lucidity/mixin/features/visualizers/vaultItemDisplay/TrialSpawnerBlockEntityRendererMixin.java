package ml.mypals.lucidity.mixin.features.visualizers.vaultItemDisplay;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.TrialSpawnerRenderer;
import net.minecraft.client.renderer.blockentity.VaultRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.FeatureToggle.VAULT_ITEM_DISPLAY;

@Mixin(TrialSpawnerRenderer.class)
public class TrialSpawnerBlockEntityRendererMixin {

    @Shadow @Final private EntityRenderDispatcher entityRenderer;


    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/SpawnerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(target = "Lnet/minecraft/client/renderer/blockentity/SpawnerRenderer;submitEntityInSpawner(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;FFLnet/minecraft/client/renderer/state/CameraRenderState;)V",
                    value = "INVOKE",
                    shift = At.Shift.AFTER
            ))
    public void render(SpawnerRenderState spawnerRenderState,
                       PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector,
                       CameraRenderState cameraRenderState,
                       CallbackInfo ci

                       ) {


        if (VAULT_ITEM_DISPLAY.getBooleanValue()) {
            poseStack.pushPose();
            poseStack.translate(0, 0.4F + 1.0F, 0);
            if(spawnerRenderState.displayEntity != null){
                SpawnerRenderer.submitEntityInSpawner(poseStack, submitNodeCollector, spawnerRenderState.displayEntity, this.entityRenderer, spawnerRenderState.spin, spawnerRenderState.scale,cameraRenderState);
            }
            poseStack.popPose();
        }
    }
}
