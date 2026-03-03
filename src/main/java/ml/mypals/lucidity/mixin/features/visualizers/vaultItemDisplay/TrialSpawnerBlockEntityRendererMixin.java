package ml.mypals.lucidity.mixin.features.visualizers.vaultItemDisplay;

//? if >=1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
//?}
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.TrialSpawnerRenderer;
import net.minecraft.client.renderer.blockentity.VaultRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
//? if >=1.21.4 {
import net.minecraft.client.renderer.entity.state.EntityRenderState;
//?}
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
//? if >=1.21.6 {
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
//?} else {
/*import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
*///?}
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


    //? if >=1.21.9 {
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


    //?} else if >=1.21.5 {
    /*@Inject(method = "render(Lnet/minecraft/world/level/block/entity/TrialSpawnerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/phys/Vec3;)V",
            at = @At(target = "Lnet/minecraft/client/renderer/blockentity/SpawnerRenderer;renderEntityInSpawner(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;DD)V",
                    value = "INVOKE",
                    shift = At.Shift.AFTER
            ))
    public void render(TrialSpawnerBlockEntity trialSpawnerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Vec3 vec3, CallbackInfo ci, @Local Level level, @Local TrialSpawner trialSpawner,
                       //? if >=1.21.6 {
                       @Local TrialSpawnerStateData trialSpawnerData
                       //?} else {
                       /^@Local TrialSpawnerData trialSpawnerData
                       ^///?}
    ) {

    *///?} else {
    
    /*@Inject(method = "render(Lnet/minecraft/world/level/block/entity/TrialSpawnerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
    at = @At(target = "Lnet/minecraft/client/renderer/blockentity/SpawnerRenderer;renderEntityInSpawner(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;DD)V",
             value = "INVOKE",
             shift = At.Shift.AFTER
    ))
    public void render(TrialSpawnerBlockEntity trialSpawnerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci, @Local Level level, @Local TrialSpawner trialSpawner, @Local TrialSpawnerData trialSpawnerData) {

    *///?}
        if (VAULT_ITEM_DISPLAY.getBooleanValue()) {
            poseStack.pushPose();
            poseStack.translate(0, 0.4F + 1.0F, 0);
            //? if >=1.21.9 {
            if(spawnerRenderState.displayEntity != null){
                SpawnerRenderer.submitEntityInSpawner(poseStack, submitNodeCollector, spawnerRenderState.displayEntity, this.entityRenderer, spawnerRenderState.spin, spawnerRenderState.scale,cameraRenderState);
            }
            //?} else {
            
            /*Entity entity = trialSpawnerData.getOrCreateDisplayEntity(trialSpawner, level, trialSpawner.getState());
            SpawnerRenderer.renderEntityInSpawner(f, poseStack, multiBufferSource, i, entity, this.entityRenderer, trialSpawnerData.getOSpin(), trialSpawnerData.getSpin());
            *///?}
            poseStack.popPose();
        }
    }
}
