package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentBuffersWrapper;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingSubmitNodeStorage;
import net.minecraft.client.renderer.MultiBufferSource;
//? if >=1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
//?}
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    // 原本这里在"透明度为 0"时直接让 shouldRender 返回 false，把隐藏实体整个剔除掉。
    // 现在透明度一律作为颜色处理，alpha=0 自身就是全透明，隐藏实体照常走半透明路径即可，
    // 不再需要这个特例（留着反而会造成 0 和 1 之间的行为断崖）。
    //? if >=1.21.9 {

    @WrapMethod(method = "submit")
    private void renderEntity(EntityRenderState entityRenderState, CameraRenderState cameraRenderState, double d, double e, double f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Operation<Void> original) {
        if (!SelectiveRenderingManager.shouldRenderEntity(entityRenderState.entityType,new Vec3(entityRenderState.x,entityRenderState.y,entityRenderState.z))){
            original.call(entityRenderState, cameraRenderState, d, e, f, poseStack,new SelectiveRenderingSubmitNodeStorage(submitNodeCollector));
        }else {
            original.call(entityRenderState, cameraRenderState, d, e, f, poseStack, submitNodeCollector);
        }
    }
    //?} else if >=1.21.3 {
    
    /*@WrapMethod(method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void renderEntity(Entity entity, double x, double y, double z, float delta, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Void> original) {
        if (!SelectiveRenderingManager.shouldRenderEntity(entity.getType(),entity.position())){
            original.call(entity, x, y, z, delta, poseStack, new ControllableTransparentBuffersWrapper((MultiBufferSource.BufferSource) multiBufferSource), i);
        }else {
            original.call(entity, x, y, z, delta, poseStack, multiBufferSource, i);
        }
    }
    *///?} else {
    /*@WrapMethod(method = "render")
    private void renderEntity(Entity entity, double x, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Void> original) {
        if (!SelectiveRenderingManager.shouldRenderEntity(entity.getType(),entity.position())){
            original.call(entity, x, e, f, g, h, poseStack,  new ControllableTransparentBuffersWrapper((MultiBufferSource.BufferSource) multiBufferSource), i);
        }else {
            original.call(entity, x, e, f, g, h, poseStack, multiBufferSource, i);
        }
    }
    *///?}
}
