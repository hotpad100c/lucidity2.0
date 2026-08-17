package ml.mypals.lucidity.flashback;

import com.moulberry.flashback.keyframe.KeyframeRegistry;

/**
 * 唯一直接接触 Flashback API 的注册入口。只应由 {@link FlashbackCompat#init()} 在
 * 确认 Flashback 已加载之后调用。
 */
final class FlashbackIntegration {

    private FlashbackIntegration() {}

    static void register() {
        KeyframeRegistry.register(SelectiveRenderingKeyFrameType.INSTANCE);
    }
}
