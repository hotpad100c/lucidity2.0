package ml.mypals.lucidity.mixin.features.visualizers.packetVisualize;

import ml.mypals.lucidity.utils.LucidityColorHelper;
import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.box.BoxShape;
import ml.mypals.ryansrenderingkit.shape.box.BoxWireframeShape;
import ml.mypals.ryansrenderingkit.shape.minecraftBuiltIn.TextShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.FeatureToggle.BLOCK_EVENT_VISUALIZE;
import static ml.mypals.lucidity.config.VisualizerColors.BLOCK_EVENT_COLOR;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow private ClientLevel level;

    @Inject(method = "handleBlockEvent", at = @At("HEAD"))
    public void onBlockEvent(ClientboundBlockEventPacket packet, CallbackInfo ci) {
        if(BLOCK_EVENT_VISUALIZE.getBooleanValue()) {
            BoxWireframeShape boxShape = ShapeGenerator.generateBoxWireframe()
                    .construction(BoxShape.BoxConstructionType.CORNERS)
                    .aabb(packet.getPos().getCenter().subtract(0.5,0.5,0.5),
                            packet.getPos().getCenter().add(0.5,0.5,0.5))
                    .transform((boxTransformer -> {
                        Shape shape = boxTransformer.getShape();
                        if(!BLOCK_EVENT_VISUALIZE.getBooleanValue()){
                            shape.discard();
                            return;
                        }
                        int life = shape.getCustomData("life",0);
                        if(life > 500){
                            shape.discard();
                        }
                        life++;
                        shape.putCustomData("life",life);

                    }))
                    .color(LucidityColorHelper.c4f2C(BLOCK_EVENT_COLOR.getColor()))
                    .seeThrough(true)
                    .edgeWidth(3)
                    .build(Shape.RenderingType.BATCH);
            ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"block_event_"+packet.getPos().hashCode()),
            boxShape);
            TextShape textShape = ShapeGenerator.generateText()
                    .texts(packet.getBlock().getName().getString(),"Type:"+packet.getB0(),"Data:"+packet.getB1())
                    .pos(packet.getPos().above().getCenter())
                    .shadow(false)
                    .outline(false)
                    .billBoardMode(TextShape.BillBoardMode.ALL)
                    .seeThrough(true)
                    .transform((boxTransformer -> {
                        Shape shape = boxTransformer.getShape();
                        if(!BLOCK_EVENT_VISUALIZE.getBooleanValue()){
                            shape.discard();
                            return;
                        }
                        int life = shape.getCustomData("life",0);
                        life++;
                        shape.putCustomData("life",life);
                        if(life > 500){
                            shape.discard();
                        }
                    }))
                    .build();
            ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"block_event_text_"+packet.getPos().hashCode()),
                    textShape);
        }
    }
}
