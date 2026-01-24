package ml.mypals.lucidity.mixin.features.visualizers.creakingHeart;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

//? if >= 1.21.4 {
import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.lucidity.utils.LucidityColorHelper;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.VisualizerColors.CREAKING_HEART_INDICATOR_COLOR;
import static ml.mypals.lucidity.config.FeatureToggle.CREAKING_HEART_INDICATOR;
import static ml.mypals.lucidity.utils.LucidityColorHelper.pulseColor;

@Mixin(ClientPacketListener.class)
//?} else {
/*@Mixin(Minecraft.class)
*///?}
public class ParticleHandlerMixin {
    //? if >= 1.21.4 {
    @Shadow private volatile boolean closed;

    @Inject(method = "handleParticleEvent", at = @At(target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)V",value="INVOKE"))
    private void lucidity$onHandleAddParticle(ClientboundLevelParticlesPacket clientboundLevelParticlesPacket, CallbackInfo ci){
        if(CREAKING_HEART_INDICATOR.getBooleanValue() &&
           clientboundLevelParticlesPacket.getParticle() instanceof TrailParticleOption trailParticleOption &&
           BuiltInRegistries.PARTICLE_TYPE.getId(clientboundLevelParticlesPacket.getParticle().getType()) == BuiltInRegistries.PARTICLE_TYPE.getId(ParticleTypes.TRAIL)&&
           trailParticleOption.color() == 6250335
        )
        {

            Vec3 targetPos = trailParticleOption.target();
            BlockPos pos = BlockPos.containing(targetPos);
            ShapeManagers.addShape(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "creaking_heart_pos_"+pos.hashCode()),
                    ShapeGenerator.generateBoxFace()
                            .aabb(pos.getCenter().subtract(0.5),pos.getCenter().add(0.5))
                            .color(0)
                            .seeThrough(true)
                            .transform(t -> {
                                Minecraft minecraft = Minecraft.getInstance();
                                Shape shape = t.getShape();
                                shape.putCustomData("lifeTime",shape.getCustomData("lifeTime",0)+1);
                                if(
                                   !CREAKING_HEART_INDICATOR.getBooleanValue()
                                   || minecraft.level == null || minecraft.player == null
                                   || shape.getCustomData("lifeTime",0) > 1200
                                   || minecraft.player.getEyePosition().distanceTo(t.getWorldPivot()) < 3
                                ){
                                    shape.discard();
                                    return;
                                }
                                t.getShape().setBaseColor(pulseColor(
                                        LucidityColorHelper.c4f2C(CREAKING_HEART_INDICATOR_COLOR.getColor()),
                                        minecraft.level.getGameTime(),
                                        0.3f
                                ));
                            })
                            .build(Shape.RenderingType.BATCH)
            );

        }
    }
    //?}
}
