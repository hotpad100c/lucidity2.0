package ml.mypals.lucidity.flashback;

import ml.mypals.lucidity.Lucidity;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Flashback 的可选兼容入口。
 *
 * 这个类刻意不引用任何 com.moulberry.* 的东西：真正接触 Flashback 的代码全在
 * {@link FlashbackIntegration} 里，只有在检测到 Flashback 之后才会被引用到，
 * 因此没装 Flashback 时那个类根本不会被加载，也就不会有 NoClassDefFoundError。
 */
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
