package ml.mypals.lucidity.features.visualizers.dragonWaypoints;

import ml.mypals.lucidity.utils.LucidityColorHelper;
import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.box.BoxFaceShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.VisualizerColors.ENDER_DRAGON_WAYPOINT_COLOR;
import static ml.mypals.lucidity.config.FeatureToggle.ENDER_DRAGON_WAYPOINTS_VISUALIZE;
import static ml.mypals.lucidity.utils.LucidityColorHelper.invertColor;

public class DragonWaypointManager {

    private static boolean needUpdate;
    private static final BlockPos[] nodeBasePos = new BlockPos[24];
    private static final BoxFaceShape[] nodeIndicators = new BoxFaceShape[24];
    public static void tick(){

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if(ENDER_DRAGON_WAYPOINTS_VISUALIZE.getBooleanValue() && !needUpdate &&  level != null && level.dimension() == Level.END){
            if(mc.isSingleplayer() && mc.getSingleplayerServer()!=null){
                level = mc.getSingleplayerServer().getLevel(Level.END);
            }
            if(!isNodesAccurate(level)){
                clearNodesIfNeeded();
            }
            if(nodeIndicators[0] == null) {
                caculateNode(level);
            }

        }else{
            clearNodesIfNeeded();
        }
    }
    public static boolean isNodesAccurate(Level level){

        if(nodeIndicators[0] == null) return false;
        boolean needUpdate = false;
        for (int i = 0; i < 24; ++i) {
            int j = 5;
            if (i >= 12 && i < 20) j += 10;

            BlockPos base = nodeBasePos[i];
            if (base == null) continue;

            int expectedY = Math.max(
                    73,
                    level.getHeightmapPos(
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            base
                    ).getY() + j
            );

            int actualY = Mth.floor(nodeIndicators[i].getShapeCenterPos().y);

            if (actualY != expectedY) {
                needUpdate = true;
                break;
            }
        }
        return !needUpdate;
    }
    public static void clearNodesIfNeeded(){
        if(nodeIndicators[0] != null){
            for(int i = 0; i < 24; ++i) {
                nodeIndicators[i].discard();
                nodeIndicators[i] = null;
            }
        }
    }
    public static void caculateNode(Level level) {
        if (level != null && nodeIndicators[0] == null) {
            for(int i = 0; i < 24; ++i) {
                int j = 5;
                int l;
                int m;
                if (i < 12) {
                    l = Mth.floor(60.0F * Mth.cos(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
                    m = Mth.floor(60.0F * Mth.sin(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
                } else if (i < 20) {
                    int k = i - 12;
                    l = Mth.floor(40.0F * Mth.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)k)));
                    m = Mth.floor(40.0F * Mth.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)k)));
                    j += 10;
                } else {
                    int var7 = i - 20;
                    l = Mth.floor(20.0F * Mth.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)var7)));
                    m = Mth.floor(20.0F * Mth.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)var7)));
                }

                int n = Math.max(73, level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, m)).getY() + j);

                boolean isOuter = i < 12;
                Color color = LucidityColorHelper.c4f2C(ENDER_DRAGON_WAYPOINT_COLOR.getColor());
                if(isOuter) color = invertColor(color.getRGB());
                Vec3 pos = new Vec3(l,n,m);
                nodeBasePos[i] = new BlockPos(l, 0, m);
                nodeIndicators[i] = ShapeGenerator
                        .generateBoxFace()
                        .size(Vec3.ZERO.add(1,1,1))
                        .seeThrough(true)
                        .aabb(pos,pos.add(1,1,1))
                        .color(color)
                        .build(Shape.RenderingType.BATCH);
                ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"ender_dragon_node_"+i),nodeIndicators[i]);
            }
        }
    }
}
