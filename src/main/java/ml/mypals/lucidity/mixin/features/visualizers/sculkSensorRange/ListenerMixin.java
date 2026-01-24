package ml.mypals.lucidity.mixin.features.visualizers.sculkSensorRange;

import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import ml.mypals.ryansrenderingkit.utils.Helpers;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

import static ml.mypals.lucidity.config.FeatureToggle.VIBRATION_TRACE;

@Mixin(VibrationSystem.Listener.class)
public abstract class ListenerMixin {
    @Inject(method =
            "isOccluded", at = @At("HEAD"))
    private static void isOccluded(Level level, Vec3 vec3, Vec3 vec32, CallbackInfoReturnable<Boolean> cir) {
        if(VIBRATION_TRACE.getBooleanValue())
            isOccluded(level,vec3,vec32);
    }
    @Unique
    private static void spawnLine(Vec3 s, Vec3 e, boolean hit){
        ShapeManagers.addShape(
                Helpers.generateUniqueId("listener_"),
                ShapeGenerator.generateLine()
                .start(s)
                .end(e)
                .lineWidth(5.0F)
                .color(hit? Color.RED:Color.GRAY)
                .seeThrough(true)
                .transform((t) -> {
                    Level level = Minecraft.getInstance().level;
                    if(level == null || t.getWidth(false) <= 0){
                        t.getShape().discard();
                        return;
                    }

                    float time = level.getGameTime();
                    if(time % 2 == 0 && !level.tickRateManager().isFrozen()){
                        t.setWidth(
                                Math.max(0,t.getWidth(false)
                                        -0.3f)
                        );
                    }
                })
                .build(Shape.RenderingType.BATCH)

        );
    }
    @Unique
    private static void isOccluded(Level level, Vec3 vec3, Vec3 vec32) {
        Vec3 vec33 = new Vec3((double)Mth.floor(vec3.x) + (double)0.5F, (double)Mth.floor(vec3.y) + (double)0.5F, (double)Mth.floor(vec3.z) + (double)0.5F);
        Vec3 vec34 = new Vec3((double)Mth.floor(vec32.x) + (double)0.5F, (double)Mth.floor(vec32.y) + (double)0.5F, (double)Mth.floor(vec32.z) + (double)0.5F);
        BlockHitResult blockHitResult = level.isBlockInLine(
                new ClipBlockStateContext(
                        vec33,
                        vec34,
                        (blockState) -> blockState.is(
                                BlockTags.OCCLUDES_VIBRATION_SIGNALS
                        )
                )
        );
        boolean hit = blockHitResult.getType() != HitResult.Type.BLOCK;
        spawnLine(vec33,blockHitResult.getLocation(),hit);
    }
}

