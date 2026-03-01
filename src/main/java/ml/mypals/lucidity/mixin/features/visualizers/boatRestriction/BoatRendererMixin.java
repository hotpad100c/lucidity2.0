package ml.mypals.lucidity.mixin.features.visualizers.boatRestriction;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
//? if >=1.21.3 {

import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.world.entity.vehicle.AbstractBoat;
//?} else {
/*import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.client.renderer.entity.BoatRenderer;
*///?}
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

//? if >=1.21.3 {
@Mixin(AbstractBoatRenderer.class)
public abstract class BoatRendererMixin extends EntityRenderer<AbstractBoat, BoatRenderState> {
//?} else {
/*@Mixin(BoatRenderer.class)
public abstract class BoatRendererMixin extends EntityRenderer<Boat> {
*///?}
    protected BoatRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    //? if >=1.21.3 {
    @Inject(
            method = "render(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/AbstractBoatRenderer;renderTypeAdditions(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    public void render(BoatRenderState boatRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
    //?} else {
        /*@Inject(
                method = "Lnet/minecraft/client/renderer/entity/BoatRenderer;render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/client/model/ListModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
                )
        )
        public void render(Boat boat, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {

    *///?}
        if(BOAT_VIEW_RESTRICTION.getBooleanValue()){
            Vec3 center = Vec3.ZERO;
            float leftYaw =  -90 - 105.0F;
            float rightYaw = -90 + 105.0F;
            Vec3 left = center.add(dirFromYaw(leftYaw).scale(2.0));
            Vec3 right = center.add(dirFromYaw(rightYaw).scale(2.0));

            List<Vec3> arc = generateArc(center, leftYaw, rightYaw, 2.0);

            BufferBuilder stripConsumer = Tesselator.getInstance().begin(RenderType.LINE_STRIP.mode(), RenderType.LINE_STRIP.format());
            addCurve(Color.WHITE,poseStack,stripConsumer ,arc);
            RenderType.LINE_STRIP.draw(stripConsumer.build());
            BufferBuilder lineConsumer = Tesselator.getInstance().begin(RenderType.LINES.mode(), RenderType.LINES.format());
            addLineSegment(Color.RED,poseStack,lineConsumer ,center,left);
            addLineSegment(Color.GREEN,poseStack,lineConsumer ,center,right);
            RenderType.LINES.draw(lineConsumer.build());
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
    private void addLineSegment(Color color,PoseStack pose, BufferBuilder consumer, Vec3 start, Vec3 end) {
        double dx = end.x() - start.x();
        double dy = end.y() - start.y();
        double dz = end.z() - start.z();
        double distanceInv = (double)1.0F / Math.sqrt(dx * dx + dy * dy + dz * dz);
        Vec3 normal = new Vec3(dx * distanceInv, dy * distanceInv, dz * distanceInv);
        //? if >=1.21.3 {
        consumer.addVertex(pose.last(), start.toVector3f()).setNormal(pose.last(), normal.toVector3f()).setColor(color.getRGB());
        consumer.addVertex(pose.last(), end.toVector3f()).setNormal(pose.last(), normal.toVector3f()).setColor(color.getRGB());
        //?} else {
        /*consumer.addVertex(pose.last(), start.toVector3f()).setNormal(pose.last(), (float) normal.x(), (float) normal.y(), (float) normal.y()).setColor(color.getRGB());
        consumer.addVertex(pose.last(), end.toVector3f()).setNormal(pose.last(),  (float) normal.x(), (float) normal.y(), (float) normal.y()).setColor(color.getRGB());
        *///?}
        }
    @Unique
    private void addCurve(Color color,PoseStack pose, BufferBuilder consumer, List<Vec3> points ) {

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
            //? if >=1.21.3 {
            consumer.addVertex(pose.last(), pos.toVector3f()).setNormal(pose.last(), normal.toVector3f()).setColor(color.getRGB());
            //?} else {
            /*consumer.addVertex(pose.last(), pos.toVector3f()).setNormal(pose.last(), (float) normal.x(), (float) normal.y(), (float) normal.y()).setColor(color.getRGB());
            *///?}
        }
    }
    
}
