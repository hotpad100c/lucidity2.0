package ml.mypals.lucidity.features.visualizers;

import ml.mypals.lucidity.features.netherPosCaculator.NetherPosCaculatorManager;
import ml.mypals.lucidity.features.visualizers.dragonWaypoints.DragonWaypointManager;
public class VisualizerManager {
    public static void tick() {
        DragonWaypointManager.tick();
        NetherPosCaculatorManager.tick();
    }
}
