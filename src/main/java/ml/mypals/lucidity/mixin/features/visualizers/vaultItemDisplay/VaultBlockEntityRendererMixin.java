package ml.mypals.lucidity.mixin.features.visualizers.vaultItemDisplay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.VaultRenderer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultClientData;
import net.minecraft.world.phys.Vec3;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.VaultRenderState;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import static ml.mypals.lucidity.config.FeatureToggle.VAULT_ITEM_DISPLAY;

@Mixin(VaultRenderer.class)
public class VaultBlockEntityRendererMixin {
    @Shadow @Final private RandomSource random;
    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/VaultRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
                    value = "INVOKE",
                    shift = At.Shift.AFTER
            ))

    public void submit(VaultRenderState vaultRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (VAULT_ITEM_DISPLAY.getBooleanValue()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.4F + 1.0F, 0.5);
            poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());

            if(vaultRenderState.displayItem != null) {
                ItemEntityRenderer.submitMultipleFromCount(
                        poseStack,
                        submitNodeCollector,
                        vaultRenderState.lightCoords,
                        vaultRenderState.displayItem,
                        this.random
                );
            }
            poseStack.popPose();
        }
    }
}
