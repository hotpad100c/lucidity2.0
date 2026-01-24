package ml.mypals.lucidity.mixin.features.visualizers.sculkSensorRange;

import ml.mypals.lucidity.config.FeatureToggle;
import ml.mypals.lucidity.features.visualizers.sculk.SculkVisualizerManager;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.round.SphereShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.awt.*;
import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.VisualizerColors.SCULK_SENSOR_RANGE_COLOR;
import static ml.mypals.lucidity.utils.LucidityColorHelper.c4f2C;
import static ml.mypals.lucidity.features.visualizers.sculk.SculkVisualizerManager.SCULK_VISUALIZER_SEGMENTS;
import static ml.mypals.lucidity.features.visualizers.sculk.SculkVisualizerManager.isInValidRange;

@Mixin(Warden.class)
public abstract class WardenTickerMixin extends Monster implements VibrationSystem{
    protected WardenTickerMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract @NotNull User getVibrationUser();

    @Unique
    SphereShape shape;

    @Inject(method = "tick", at = @At("HEAD"))
    public void lucidity_sculk_visualizer$custom_tick(CallbackInfo ci){
        if (isInValidRange(this.getEyePosition()) && FeatureToggle.SCULK_SENSOR_VISUALIZE.getBooleanValue() && shape == null) {
            shape = new SphereShape(
                    Shape.RenderingType.BATCH,
                    t -> {
                        if(!isInValidRange(this.getEyePosition()) || this.isRemoved() || !FeatureToggle.SCULK_SENSOR_VISUALIZE.getBooleanValue()){
                            this.shape = null;
                            t.getShape().discard();
                        }
                        t.setShapeWorldPivot(this.getPosition(1));
                        t.getShape().setBaseColor(c4f2C(SCULK_SENSOR_RANGE_COLOR.getColor()));
                    },
                    this.getPosition(0),
                    SCULK_VISUALIZER_SEGMENTS,
                    this.getVibrationUser().getListenerRadius(),
                    new Color(84, 154, 172,100),
                    false
            );
            SculkVisualizerManager.getVisualizers().add(shape);
            ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,this.getUUID().toString().toLowerCase()), shape);
        }
    }
}

