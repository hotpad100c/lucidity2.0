package ml.mypals.lucidity.mixin.features.accurateShadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.client.render.immediate.model.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static ml.mypals.lucidity.config.FeatureToggle.ACCURATE_ENTITY_SHADOW;

@Mixin(EntityRenderDispatcher.class)
public class EntityRendererMixin {
    @WrapOperation(method = "renderBlockShadow",at = @At(
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shadowVertex(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;IFFFFF)V"
            ,value = "INVOKE"
    ))
    private static void shadowVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, float f, float g, float h, float j, float k, Operation<Void> original, @Local AABB aABB) {
        h = ACCURATE_ENTITY_SHADOW.getBooleanValue()?h+(float)(aABB.maxY):h;
        Vector3f vector3f = pose.pose().transformPosition(f, g, h, new Vector3f());
        vertexConsumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z(), i, j, k, OverlayTexture.NO_OVERLAY, 15728880, 0.0F, 1.0F, 0.0F);
    }
}
