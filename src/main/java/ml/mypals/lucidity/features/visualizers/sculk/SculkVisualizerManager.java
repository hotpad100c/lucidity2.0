package ml.mypals.lucidity.features.visualizers.sculk;

import ml.mypals.ryansrenderingkit.shape.round.SphereShape;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static ml.mypals.lucidity.config.LucidityConfigs.Generic.SCULK_DETECT_RANGE;

public class SculkVisualizerManager {
    public final static int SCULK_VISUALIZER_SEGMENTS = 16;
    private static ArrayList<SphereShape> visualizers = new ArrayList<>();
    public static List<SphereShape> getVisualizers() {
        return visualizers;
    }
    public static void addVisualizer(SphereShape visualizer) {
        visualizers.add(visualizer);
    }
    public static void clearVisualizers() {
        visualizers.clear();
    }
    public static boolean isInValidRange(Vec3 center) {
        if(Minecraft.getInstance().player == null) {return false;}
        return center.distanceTo(Minecraft.getInstance().player.getEyePosition()) <= SCULK_DETECT_RANGE.getIntegerValue();
    }
    public static void onToggle(boolean enabled) {
        if(!enabled) {
            for(SphereShape sculkVisualizer : visualizers) {
                sculkVisualizer.discard();
            }
            clearVisualizers();
        }
    }
}
