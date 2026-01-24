package ml.mypals.lucidity.mixin.features.visualizers.enderDragonDestructionRange;

import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.box.BoxFaceShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.VisualizerColors.ENDER_DRAGON_DESTRUCTION_RANGE_COLOR;
import static ml.mypals.lucidity.config.FeatureToggle.ENDER_DRAGON_DESTRUCTION_VISUALIZE;
import static ml.mypals.lucidity.utils.LucidityColorHelper.c4f2C;
import static ml.mypals.lucidity.utils.LucidityRenderUtils.renderBox;
import static ml.mypals.lucidity.utils.LucidityMiscHelpers.shouldProcessEntityIfSinglePlayerOrOnlyClientSide;

@Mixin(EnderDragon.class)
public abstract class EnderdragonMixin extends Mob implements Enemy {
    @Shadow @Final private EnderDragonPart body;
    @Shadow @Final private EnderDragonPart neck;
    @Shadow @Final public EnderDragonPart head;
    @Shadow @Final private EnderDragonPart[] subEntities;
    @Unique
    private BoxFaceShape headDestructionBox;
    @Unique
    private BoxFaceShape neckDestructionBox;
    @Unique
    private BoxFaceShape bodyDestructionBox;

    protected EnderdragonMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }@Inject(method = "aiStep", at = @At("HEAD"))
    private void aiStep(CallbackInfo ci) {
        boolean enabled = ENDER_DRAGON_DESTRUCTION_VISUALIZE.getBooleanValue();
        if(!enabled) {
            if(bodyDestructionBox != null && bodyDestructionBox.enabled) {
                bodyDestructionBox.enabled = false;
            }
            if(neckDestructionBox != null && neckDestructionBox.enabled) {
                neckDestructionBox.enabled = false;
            }
            if(headDestructionBox != null && headDestructionBox.enabled) {
                headDestructionBox.enabled = false;
            }
            return;
        }

        if (shouldProcessEntityIfSinglePlayerOrOnlyClientSide(this)) {
            if(bodyDestructionBox != null) {
                updateDestructionBox(bodyDestructionBox, this.body.getBoundingBox());
            }else {
                bodyDestructionBox = ShapeGenerator.generateBoxFace()
                        .color(0)
                        .transform(boxTransformer ->{
                            if(this.isDeadOrDying() || this.isRemoved()){
                                boxTransformer.getShape().discard();
                            }
                        })
                        .build(Shape.RenderingType.BATCH);
                ShapeManagers.addShape(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "enderdragon_destruction_box_body_"
                                + this.body.getStringUUID().toLowerCase()),
                        bodyDestructionBox
                );
            }
            if(neckDestructionBox != null) {
                updateDestructionBox(neckDestructionBox, this.neck.getBoundingBox());
            }else {
                neckDestructionBox = ShapeGenerator.generateBoxFace()
                        .color(0)
                        .transform(boxTransformer ->{
                            if(this.isDeadOrDying() || this.isRemoved()){
                                boxTransformer.getShape().discard();
                            }
                        })
                        .build(Shape.RenderingType.BATCH);
                ShapeManagers.addShape(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "enderdragon_destruction_box_neck_"
                                + this.neck.getStringUUID().toLowerCase()),
                        neckDestructionBox
                );
            }
            if(headDestructionBox != null) {
                updateDestructionBox(headDestructionBox, this.head.getBoundingBox());
            }else {
                headDestructionBox = ShapeGenerator.generateBoxFace()
                        .color(0)
                        .transform(boxTransformer ->{
                            if(this.isDeadOrDying() || this.isRemoved()){
                                boxTransformer.getShape().discard();
                            }
                        })
                        .build(Shape.RenderingType.BATCH);
                ShapeManagers.addShape(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "enderdragon_destruction_box_head_"
                                + this.head.getStringUUID().toLowerCase()),
                        headDestructionBox
                );
            }
        }
    }

    @Unique
    private void updateDestructionBox(BoxFaceShape shape, AABB partAABB) {
        if (shape == null) return;
        AABB destructionAABB = getDestructionAABB(partAABB);
        shape.enabled = true;
        shape.setBaseColor(c4f2C(ENDER_DRAGON_DESTRUCTION_RANGE_COLOR.getColor()));
        shape.forceSetCorners(destructionAABB.getMinPosition(), destructionAABB.getMaxPosition());
    }

    @Unique
    private AABB getDestructionAABB(AABB partAABB) {
        int minBlockX = Mth.floor(partAABB.minX);
        int minBlockY = Mth.floor(partAABB.minY);
        int minBlockZ = Mth.floor(partAABB.minZ);
        int maxBlockX = Mth.floor(partAABB.maxX);
        int maxBlockY = Mth.floor(partAABB.maxY);
        int maxBlockZ = Mth.floor(partAABB.maxZ);

        return new AABB(
                minBlockX,
                minBlockY,
                minBlockZ,
                maxBlockX + 1,
                maxBlockY + 1,
                maxBlockZ + 1
        );
    }
}
