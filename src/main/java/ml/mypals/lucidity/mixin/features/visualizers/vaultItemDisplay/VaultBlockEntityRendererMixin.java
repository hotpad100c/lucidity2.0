package ml.mypals.lucidity.mixin.features.visualizers.vaultItemDisplay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.VaultRenderer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
//? if >=1.21.4 {
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;

import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultClientData;
import net.minecraft.world.phys.Vec3;
import com.llamalad7.mixinextras.sugar.Local;
//?}
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import static ml.mypals.lucidity.config.FeatureToggle.VAULT_ITEM_DISPLAY;

@Mixin(VaultRenderer.class)
public class VaultBlockEntityRendererMixin {
    //? if >=1.21.4 {
    @Shadow @Final private ItemClusterRenderState renderState;
    //?}
    @Shadow @Final private RandomSource random;
    //? if >=1.21.5 {
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/vault/VaultBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/phys/Vec3;)V",
            at = @At(target = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;renderMultipleFromCount(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;Lnet/minecraft/util/RandomSource;)V",
                    value = "INVOKE",
                    shift = At.Shift.AFTER
            ))
    public void render(VaultBlockEntity vaultBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Vec3 vec3, CallbackInfo ci, @Local Level level, @Local VaultClientData vaultClientData) {

    //?} else {
        /*//? if >=1.21.4 {
        @Inject(method = "render(Lnet/minecraft/world/level/block/entity/vault/VaultBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        //?} else {
        /^@Inject(method = "renderItemInside(FLnet/minecraft/world/level/Level;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/entity/ItemRenderer;FFLnet/minecraft/util/RandomSource;)V",
        ^///?}
        at = @At(target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
                 value = "INVOKE",
                 shift = At.Shift.AFTER
        ))
        //? if >=1.21.4 {
        private void render(VaultBlockEntity vaultBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci, @Local VaultClientData vaultClientData) {
        //?} else {
        /^private static void render(float f, Level level, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ItemStack itemStack, ItemRenderer itemRenderer, float g, float h, RandomSource randomSource, CallbackInfo ci) {
        ^///?}
    *///?}
        if (VAULT_ITEM_DISPLAY.getBooleanValue()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.4F + 1.0F, 0.5F);
            poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
            ItemEntityRenderer.renderMultipleFromCount(
                    //? if <=1.21.3 {
                    /*Minecraft.getInstance().getItemRenderer(),
                    *///?}
                    poseStack, multiBufferSource, i,
                    //? if >=1.21.4 {
                    this.renderState,
                    this.random
                    //?} else {
                    /*itemStack,
                    randomSource
                    *///?}
                    //? if <=1.21.3 {
                    /*, level
                    *///?}
            );
            poseStack.popPose();
        }
    }
}
