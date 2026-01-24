package ml.mypals.lucidity.features.visualizers.soundVisualize;

import ml.mypals.lucidity.utils.LucidityColorHelper;
import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.box.BoxShape;
import ml.mypals.ryansrenderingkit.shape.box.BoxWireframeShape;
import ml.mypals.ryansrenderingkit.shape.minecraftBuiltIn.TextShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.FeatureToggle.SOUND_VISUALIZE;
import static ml.mypals.lucidity.config.VisualizerColors.SOUND_COLOR;

public class SoundListener implements SoundEventListener {
    public static boolean registered = false;
    public static SoundListener INSTANCE = new SoundListener();
    @Override
    public void onPlaySound(@NotNull SoundInstance soundInstance, @NotNull WeighedSoundEvents weighedSoundEvents, float f) {
        if(SOUND_VISUALIZE.getBooleanValue()) {
            Vec3 pos = new Vec3(soundInstance.getX(),soundInstance.getY(),soundInstance.getZ());
            BoxWireframeShape boxShape = ShapeGenerator.generateBoxWireframe()
                    .construction(BoxShape.BoxConstructionType.CORNERS)
                    .seeThrough(true)
                    .aabb(pos.subtract(0.2,0.2,0.2),
                            pos.add(0.2,0.2,0.2))
                    .transform((boxTransformer -> {
                        Shape shape = boxTransformer.getShape();
                        if(!SOUND_VISUALIZE.getBooleanValue()){
                            shape.discard();
                            return;
                        }
                        int life = shape.getCustomData("life",0);
                        if(life > 200){
                            shape.discard();
                        }
                        life++;
                        shape.putCustomData("life",life);

                    }))
                    .color(LucidityColorHelper.c4f2C(SOUND_COLOR.getColor()))
                    .edgeWidth(3)
                    .build(Shape.RenderingType.BATCH);
            ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"block_event_"+soundInstance.hashCode()),
                    boxShape);
            TextShape textShape = ShapeGenerator.generateText()
                    .texts(weighedSoundEvents.getSubtitle() != null?weighedSoundEvents.getSubtitle().getString():"N/A")
                    .pos(pos.add(0,0.2,0))
                    .seeThrough(true)
                    .shadow(true)
                    .outline(false)
                    .billBoardMode(TextShape.BillBoardMode.ALL)
                    .transform((boxTransformer -> {
                        Shape shape = boxTransformer.getShape();
                        if(!SOUND_VISUALIZE.getBooleanValue()){
                            shape.discard();
                            return;
                        }
                        int life = shape.getCustomData("life",0);
                        life++;
                        shape.putCustomData("life",life);
                        if(life > 200){
                            shape.discard();
                        }
                    }))
                    .build();
            ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"block_event_text_"+soundInstance.hashCode()),
                    textShape);
        }
    }
}
