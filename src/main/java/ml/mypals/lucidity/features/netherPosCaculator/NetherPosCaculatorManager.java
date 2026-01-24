package ml.mypals.lucidity.features.netherPosCaculator;

import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.box.BoxShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.FeatureToggle.NETHER_COORDINATE_CACULATOR;
import static net.minecraft.world.level.Level.NETHER;
import static net.minecraft.world.level.Level.OVERWORLD;

public class NetherPosCaculatorManager {
    private static int cooldown = 0;
    public static List<CoordinatesEntry> coordinatesEntries = new ArrayList<>();
    public static void onPlayerUse(Player player, Level world, InteractionHand hand, BlockHitResult blockHitResult) {
        if(cooldown >0){
            cooldown--;
        }
        if(!NETHER_COORDINATE_CACULATOR.getBooleanValue() || cooldown>0 || !world.isClientSide() || player != Minecraft.getInstance().player || !player.getItemInHand(hand).isEmpty())return;
        CoordinatesEntry.PositionType positionType;
        if(world.dimension() == NETHER){
            positionType = CoordinatesEntry.PositionType.NETHER;
        }else if(world.dimension() == OVERWORLD){
            positionType = CoordinatesEntry.PositionType.OVERWORLD;
        }else {
            return;
        }
        List<CoordinatesEntry> toRemove = new ArrayList<>();
        for(CoordinatesEntry coordinatesEntry : coordinatesEntries){
            if(coordinatesEntry.alreadyShowing(blockHitResult.getBlockPos().immutable(),positionType)){
                coordinatesEntry.remove();
                toRemove.add(coordinatesEntry);
            }
        }
        if(toRemove.isEmpty()){
            CoordinatesEntry coordinatesEntry = new CoordinatesEntry(blockHitResult.getBlockPos().immutable(),positionType);
            coordinatesEntry.submit();
            coordinatesEntries.add(coordinatesEntry);

        }else {
            coordinatesEntries.removeAll(toRemove);
        }

        player.swing(hand);
        cooldown = 2;
    }

    public static void tick(){
        if(NETHER_COORDINATE_CACULATOR.getBooleanValue()){
            for(CoordinatesEntry coordinatesEntry : coordinatesEntries){
                coordinatesEntry.tick();
            }
        }else{
            if(coordinatesEntries.isEmpty()) return;
            for(CoordinatesEntry coordinatesEntry : coordinatesEntries){
                coordinatesEntry.remove();
            }
            coordinatesEntries.clear();
        }
    }
    public static class CoordinatesEntry{
        public static enum PositionType{
            OVERWORLD,
            NETHER
        }
        public Color color;
        public BlockPos netherpos;
        public BlockPos overworldPos;
        public BoxShape overworldPosInd;
        public BoxShape netherPosInd;
        public CoordinatesEntry(BlockPos pos, PositionType positionType){
            ThreadLocalRandom random = ThreadLocalRandom.current();
            color = new Color(random.nextInt(150,255),random.nextInt(150,255),random.nextInt(150,255),100);
            switch (positionType){
                case OVERWORLD -> {
                    netherpos = BlockPos.containing((double) pos.getX() /8, (double) pos.getY(), (double) pos.getZ() /8);
                    overworldPos = pos;
                }
                case NETHER -> {
                    overworldPos = BlockPos.containing((double) pos.getX() *8, (double) pos.getY(), (double) pos.getZ() *8);
                    netherpos = pos;
                }default -> {
                    netherpos = BlockPos.containing((double) pos.getX() /8, (double) pos.getY(), (double) pos.getZ() /8);
                    overworldPos = pos;
                }
            }

            overworldPosInd = ShapeGenerator.generateBoxFace()
                    .size(new Vec3(1,1,1))
                    .aabb(overworldPos.getCenter().subtract(0.5,0.5,0.5),overworldPos.getCenter().add(0.5,0.5,0.5))
                    .color(color)
                    .seeThrough(true)
                    .transform(transformer->{
                        float size = scaleByDistance(overworldPos.getCenter(),1f,20f);
                        transformer.setDimension(new Vec3(size,size,size));
                    })
                    .build(Shape.RenderingType.BATCH);

            netherPosInd = ShapeGenerator.generateBoxFace()
                    .size(new Vec3(1,1,1))
                    .aabb(netherpos.getCenter().subtract(0.5,0.5,0.5),netherpos.getCenter().add(0.5,0.5,0.5))
                    .color(color)
                    .seeThrough(true)
                    .transform(transformer->{
                        float size = scaleByDistance(netherpos.getCenter(),1f,20f);
                        transformer.setDimension(new Vec3(size,size,size));
                    })
                    .build(Shape.RenderingType.BATCH);
        }
        public boolean alreadyShowing(BlockPos pos, PositionType positionType){
            return switch (positionType){
                case OVERWORLD -> overworldPos.equals(pos);
                case NETHER -> netherpos.equals(pos);
            };
        }
        public void tick(){
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            if(overworldPosInd == null || netherPosInd == null) return;
            if(level != null && (level.dimension() == Level.OVERWORLD || level.dimension() == NETHER)) {
                if(level.dimension() == Level.OVERWORLD){
                    if(netherPosInd.enabled){
                        netherPosInd.enabled = false;
                    }
                    if(!overworldPosInd.enabled){
                        overworldPosInd.enabled = true;
                    }
                }else{
                    if(overworldPosInd.enabled){
                        overworldPosInd.enabled = false;
                    }
                    if(!netherPosInd.enabled){
                        netherPosInd.enabled = true;
                    }
                }
            }
        }
        public void remove(){
            if(overworldPosInd != null){
                overworldPosInd.discard();
                overworldPosInd = null;
            }
            if(netherPosInd != null){
                netherPosInd.discard();
                netherPosInd = null;
            }
        }
        public void submit(){
            ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"indicator_overworld_"+overworldPos.hashCode()),
                    overworldPosInd);
            ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"indicator_nether_"+netherpos.hashCode()),
                    netherPosInd);
        }
        public static float scaleByDistance(
                Vec3 pos,
                float baseSize,
                float referenceDistance
        ) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return baseSize;

            Vec3 eyePos = client.player.getEyePosition(1.0F);
            double distance = eyePos.distanceTo(pos);

            distance = Math.max(distance, 0.01);

            return Math.max(baseSize,(float) (baseSize * (distance / referenceDistance)));
        }
    }
}
