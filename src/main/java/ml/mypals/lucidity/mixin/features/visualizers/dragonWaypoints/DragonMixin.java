package ml.mypals.lucidity.mixin.features.visualizers.dragonWaypoints;

import ml.mypals.lucidity.utils.LucidityColorHelper;
import ml.mypals.lucidity.utils.LucidityMiscHelpers;
import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.box.BoxFaceShape;
import ml.mypals.ryansrenderingkit.shape.line.StripLineShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.VisualizerColors.ENDER_DRAGON_WAYPOINT_COLOR;
import static ml.mypals.lucidity.config.FeatureToggle.ENDER_DRAGON_WAYPOINTS_VISUALIZE;
@Mixin(EnderDragon.class)
public abstract class DragonMixin extends Mob implements Enemy {
    @Shadow public abstract EnderDragonPhaseManager getPhaseManager();

    @Shadow @Final private EnderDragonPart tail1;
    @Shadow @Final private EnderDragonPart tail2;
    @Shadow @Final private EnderDragonPhaseManager phaseManager;
    private BoxFaceShape curentTarget;
    private StripLineShape pathIndicator;

    protected DragonMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep",at = @At("RETURN"))
    private void aiStep(CallbackInfo ci){
        if(this.level().isClientSide() || !ENDER_DRAGON_WAYPOINTS_VISUALIZE.getBooleanValue()) return;
        DragonPhaseInstance dragonPhaseInstance = this.getPhaseManager().getCurrentPhase();
        if(dragonPhaseInstance instanceof DragonHoldingPatternPhase dragonHoldingPatternPhase){
            Vec3 target = dragonHoldingPatternPhase.getFlyTargetLocation();
            if(target != null){
                if (curentTarget == null) {
                    curentTarget = ShapeGenerator
                            .generateBoxFace()
                            .aabb(target.subtract(0.5,0.5,0.5),target.add(0.5,0.5,0.5))
                            .color(Color.yellow)
                            .seeThrough(true)
                            .transform(boxTransformer -> {
                                if(!ENDER_DRAGON_WAYPOINTS_VISUALIZE.getBooleanValue() || this.isDeadOrDying() || this.isRemoved() || !(this.getPhaseManager().getCurrentPhase() instanceof DragonHoldingPatternPhase)){
                                    boxTransformer.getShape().discard();
                                    curentTarget = null;
                                    return;
                                }
                            })
                            .build(Shape.RenderingType.BATCH);
                    ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"dragon_"+this.getUUID().toString().toLowerCase()+"_target"),curentTarget);
                }
                curentTarget.setWorldPosition(target);
                Vector3f r = LucidityMiscHelpers.rotate(this.level().getGameTime()).toVector3f();
                curentTarget.setWorldRotation(r);
            }
            Path path = dragonHoldingPatternPhase.currentPath;
            if(path != null && path.nodes.size() >=2 ){

                List<Vec3> points = new ArrayList<>();
                for(Node node : path.nodes){
                    points.add(node.asVec3());
                }
                if (pathIndicator == null) {
                    pathIndicator = ShapeGenerator
                            .generateStripLine()
                            .color(LucidityColorHelper.c4f2C(ENDER_DRAGON_WAYPOINT_COLOR.getColor()))
                            .seeThrough(true)
                            .lineWidth(3)
                            .vertexes(points)
                            .transform(simpleLineTransformer -> {
                                if(!ENDER_DRAGON_WAYPOINTS_VISUALIZE.getBooleanValue() || this.isDeadOrDying() || this.isRemoved() || dragonHoldingPatternPhase.currentPath == null || dragonHoldingPatternPhase.currentPath.nodes.size() < 2){
                                    simpleLineTransformer.getShape().discard();
                                    pathIndicator = null;
                                    return;
                                }
                            })
                            .build(Shape.RenderingType.BATCH);
                    ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"dragon_"+this.getUUID().toString().toLowerCase()+"_path"),pathIndicator);
                }
                pathIndicator.setVertexes(points);
                Color color = path.isDone()?
                         LucidityColorHelper.invertColor(ENDER_DRAGON_WAYPOINT_COLOR.getColor().intValue)
                        :LucidityColorHelper.c4f2C(ENDER_DRAGON_WAYPOINT_COLOR.getColor());
                pathIndicator.setBaseColor(color);
            }
        }
    }
}
