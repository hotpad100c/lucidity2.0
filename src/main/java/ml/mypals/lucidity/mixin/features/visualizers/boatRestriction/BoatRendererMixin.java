package ml.mypals.lucidity.mixin.features.visualizers.boatRestriction;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.utils.DeferredGeometry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.*;

import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ml.mypals.lucidity.config.FeatureToggle.BOAT_VIEW_RESTRICTION;

@Mixin(AbstractBoatRenderer.class)
public abstract class BoatRendererMixin extends EntityRenderer<AbstractBoat, BoatRenderState> {
    protected BoatRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    // 1.21.9 起 render(...) 换成 submit(...)，renderTypeAdditions 也改名为 submitTypeAdditions
    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/AbstractBoatRenderer;submitTypeAdditions(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
            )
    )
    public void render(BoatRenderState boatRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if(BOAT_VIEW_RESTRICTION.getBooleanValue()){
            Vec3 center = Vec3.ZERO;
            float leftYaw =  -90 - 105.0F;
            float rightYaw = -90 + 105.0F;
            Vec3 left = center.add(dirFromYaw(leftYaw).scale(2.0));
            Vec3 right = center.add(dirFromYaw(rightYaw).scale(2.0));

            List<Vec3> arc = generateArc(center, leftYaw, rightYaw, 2.0);

            // submit 阶段不能自己 Tesselator.begin/draw，交给提交节点在 draw 阶段执行。
            // 另外 1.21.11 移除了 LINE_STRIP，弧线拆成相邻两点的线段画进同一个 LINES 批次。
            DeferredGeometry.submit(submitNodeCollector, poseStack, RenderTypes.LINES, (ps, consumer) -> {
                for (int arcIndex = 0; arcIndex + 1 < arc.size(); arcIndex++) {
                    addLineSegment(Color.WHITE, ps, consumer, arc.get(arcIndex), arc.get(arcIndex + 1));
                }
                addLineSegment(Color.RED, ps, consumer, center, left);
                addLineSegment(Color.GREEN, ps, consumer, center, right);
            });
        }
    }
    @Unique
    private List<Vec3> generateArc(Vec3 center, float startYaw, float endYaw, double radius) {
        List<Vec3> points = new ArrayList<>();

        for (float yaw = startYaw; yaw <= endYaw; yaw += 5F) {
            double rad = Math.toRadians(yaw);

            Vec3 point = center.add(
                    -Mth.sin((float) rad) * radius,
                    0.0,
                    Mth.cos((float) rad) * radius
            );

            points.add(point);
        }

        return points;
    }


    @Unique
    private Vec3 dirFromYaw(float yawDeg) {
        double rad = Math.toRadians(yawDeg);
        return new Vec3(
                -Math.sin((float) rad),  // X
                0.0,                   // Y
                Math.cos((float) rad)   // Z
        );
    }
    @Unique
    private void addLineSegment(Color color,PoseStack pose, VertexConsumer consumer, Vec3 start, Vec3 end) {
        double dx = end.x() - start.x();
        double dy = end.y() - start.y();
        double dz = end.z() - start.z();
        double distanceInv = (double)1.0F / Math.sqrt(dx * dx + dy * dy + dz * dz);
        Vec3 normal = new Vec3(dx * distanceInv, dy * distanceInv, dz * distanceInv);
        consumer.addVertex(pose.last(), start.toVector3f()).setNormal(pose.last(), normal.toVector3f()).setColor(color.getRGB());
        consumer.addVertex(pose.last(), end.toVector3f()).setNormal(pose.last(), normal.toVector3f()).setColor(color.getRGB());
        }
    @Unique
    private void addCurve(Color color,PoseStack pose, VertexConsumer consumer, List<Vec3> points ) {

        int n = points.size();
        for(int i = 0; i < n; ++i) {
            Vec3 normal;
            if (i == 0) {
                Vec3 dir = points.get(1).subtract(points.get(0));
                normal = dir.normalize();
            } else if (i == n - 1) {
                Vec3 dir = points.get(n - 1).subtract(points.get(n - 2));
                normal = dir.normalize();
            } else {
                Vec3 prevDir = points.get(i).subtract(points.get(i - 1));
                Vec3 nextDir = points.get(i + 1).subtract(points.get(i));
                normal = prevDir.add(nextDir).normalize();
                if (Double.isNaN(normal.x) || Double.isNaN(normal.y) || Double.isNaN(normal.z)) {
                    Vec3 fallback = nextDir.lengthSqr() > (double)0.0F ? nextDir : prevDir;
                    normal = fallback.normalize();
                }
            }
            Vec3 pos = points.get(i);
            consumer.addVertex(pose.last(), pos.toVector3f()).setNormal(pose.last(), normal.toVector3f()).setColor(color.getRGB());
        }
    }
    
}
