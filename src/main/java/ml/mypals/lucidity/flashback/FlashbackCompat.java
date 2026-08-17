package ml.mypals.lucidity.flashback;

import ml.mypals.lucidity.Lucidity;
import net.fabricmc.loader.api.FabricLoader;

public final class FlashbackCompat {

    public static final String FLASHBACK_MOD_ID = "flashback";

    private static boolean registered = false;

    private FlashbackCompat() {}

    public static boolean isFlashbackLoaded() {
        return FabricLoader.getInstance().isModLoaded(FLASHBACK_MOD_ID);
    }

    public static boolean isRegistered() {
        return registered;
    }

    public static void init() {
        if (!isFlashbackLoaded()) {
            return;
        }
        try {
            FlashbackIntegration.register();
            registered = true;
            Lucidity.LOGGER.info("Flashback detected: registered the selective rendering transparency keyframe");
        } catch (Throwable throwable) {
            // Flashback 的内部结构没有兼容性承诺，版本对不上时降级而不是把游戏带崩
            Lucidity.LOGGER.warn("Failed to register the selective rendering keyframe with Flashback", throwable);
        }
    }
}
