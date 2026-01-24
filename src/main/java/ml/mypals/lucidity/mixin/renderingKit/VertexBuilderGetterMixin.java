package ml.mypals.lucidity.mixin.renderingKit;

import ml.mypals.lucidity.features.imageRender.MediaShape;
import ml.mypals.lucidity.features.visualizers.explosion.ExplosionBlockPredicateShape;
import ml.mypals.lucidity.features.visualizers.explosion.ExplosionRayShape;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shapeManagers.EmptyShapeManager;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManager;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import ml.mypals.ryansrenderingkit.shapeManagers.VertexBuilderGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.ryansrenderingkit.shapeManagers.VertexBuilderGetter.registerEmptyShapeBuilder;
import static ml.mypals.ryansrenderingkit.shapeManagers.VertexBuilderGetter.registerShapeBuilder;

@Mixin(value = VertexBuilderGetter.class,remap = false)
public abstract class VertexBuilderGetterMixin {

    @Inject(method = "init",at = @At("TAIL"))
    private static void init(CallbackInfo ci) {
        registerShapeBuilder(ExplosionBlockPredicateShape.class, ShapeManagers.TRIANGLES_SHAPE_MANAGER);
        registerShapeBuilder(ExplosionRayShape.class, ShapeManagers.LINES_SHAPE_MANAGER);
        registerEmptyShapeBuilder(MediaShape.class,ShapeManagers.NON_SHAPE_OBJECTS);
    }
}
