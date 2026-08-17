package ml.mypals.lucidity.flashback;

import com.moulberry.flashback.keyframe.change.KeyframeChange;
import com.moulberry.flashback.keyframe.handler.KeyframeHandler;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;

/**
 * KeyframeHandler 的 applyXxx 是写死的一组方法，没有给第三方留通用出口，
 * 所以这里不经过 handler，直接写回自己的配置。
 */
public record SelectiveRenderingKeyFrameChange(float transparency) implements KeyframeChange {

    @Override
    public void apply(KeyframeHandler keyframeHandler) {
        int value = Math.round(Math.max(0.0f, Math.min(255.0f, this.transparency)));
        // malilib 的 setIntegerValue 只在数值真的变化时才回调，
        // 所以关键帧之间数值不变的那些帧不会触发任何重建
        SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.setIntegerValue(value);
    }

    @Override
    public KeyframeChange interpolate(KeyframeChange other, double amount) {
        if (!(other instanceof SelectiveRenderingKeyFrameChange target)) {
            return this;
        }
        return new SelectiveRenderingKeyFrameChange(
                (float) (this.transparency + (target.transparency - this.transparency) * amount));
    }
}
