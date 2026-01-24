package ml.mypals.lucidity.mixin.features.visualizers.sculkSensorRange;

import ml.mypals.lucidity.config.FeatureToggle;
import ml.mypals.lucidity.features.visualizers.sculk.ISculkSensorBlockEntity;
import ml.mypals.lucidity.features.visualizers.sculk.SculkVisualizerManager;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.round.SphereShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.awt.*;

import static ml.mypals.lucidity.config.VisualizerColors.SCULK_SENSOR_RANGE_COLOR;
import static ml.mypals.lucidity.utils.LucidityColorHelper.c4f2C;
import static ml.mypals.lucidity.features.visualizers.sculk.SculkVisualizerManager.SCULK_VISUALIZER_SEGMENTS;
import static ml.mypals.lucidity.features.visualizers.sculk.SculkVisualizerManager.isInValidRange;

@Mixin(SculkSensorBlockEntity.class)
public abstract class SculkSensorBlockTickerMixin extends BlockEntity implements ISculkSensorBlockEntity, GameEventListener.Provider<VibrationSystem.Listener>, VibrationSystem {

    @Unique
    SphereShape shape;


    public SculkSensorBlockTickerMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public void lucidity_sculk_visualizer$custom_tick(){
        if (FeatureToggle.SCULK_SENSOR_VISUALIZE.getBooleanValue() && isInValidRange(this.getBlockPos().getCenter()) && shape == null) {
            shape = new SphereShape(
                    Shape.RenderingType.BATCH,
                    t -> {
                        if(!isInValidRange(this.getBlockPos().getCenter()) || this.isRemoved() || !FeatureToggle.SCULK_SENSOR_VISUALIZE.getBooleanValue()){
                            this.shape = null;
                            t.getShape().discard();
                        }
                        t.getShape().setBaseColor(c4f2C(SCULK_SENSOR_RANGE_COLOR.getColor()));
                    },
                    this.getBlockPos().getCenter(),
                    SCULK_VISUALIZER_SEGMENTS,
                    this.getVibrationUser().getListenerRadius(),
                    new Color(84, 154, 172,100),
                    false
            );
            SculkVisualizerManager.getVisualizers().add(shape);
            ShapeManagers.addShape(ml.mypals.ryansrenderingkit.utils.Helpers.generateUniqueId("volb_"), shape);
        }
    }
}

