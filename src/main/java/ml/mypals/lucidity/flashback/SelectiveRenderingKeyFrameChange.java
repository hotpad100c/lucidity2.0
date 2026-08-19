package ml.mypals.lucidity.flashback;

import com.moulberry.flashback.keyframe.change.KeyframeChange;
import com.moulberry.flashback.keyframe.handler.KeyframeHandler;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.AreaBox;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;

/**
 * KeyframeHandler 的 applyXxx 是写死的一组方法，没有给第三方留通用出口，
 * 所以这里不经过 handler，直接写回自己的配置。
 *
 * @param target       {@link SelectiveRenderingKeyFrame#GLOBAL_TARGET} 表示全局透明度，
 *                     否则是某个选区的对角线
 * @param transparency 目标要取的透明度（0..255）
 */
public record SelectiveRenderingKeyFrameChange(String target, float transparency) implements KeyframeChange {

    @Override
    public void apply(KeyframeHandler keyframeHandler) {
        int value = Math.round(Math.clamp(this.transparency, 0.0f, 255.0f));

        if (SelectiveRenderingKeyFrame.GLOBAL_TARGET.equals(this.target)) {
            // malilib 的 setIntegerValue 只在数值真的变化时才回调，
            // 所以关键帧之间数值不变的那些帧不会触发任何重建
            SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.setIntegerValue(value);
            return;
        }

        AreaBox area = SelectiveRenderingManager.findArea(this.target);
        if (area == null) {
            // 录制时存在、现在已经被删掉或挪走的选区，直接跳过
            return;
        }
        // 只改内存里的选区，不写 SELECTED_AREAS：写配置会重建整份选区列表，
        // 逐帧应用关键帧承受不起。setOwnTransparency 自己会判重并只重建该选区的范围。
        area.setOwnTransparency(value);
    }

    @Override
    public KeyframeChange interpolate(KeyframeChange other, double amount) {
        if (!(other instanceof SelectiveRenderingKeyFrameChange target) || !this.target.equals(target.target)) {
            // 目标不同的两个变更之间没有中间状态可言
            return this;
        }
        return new SelectiveRenderingKeyFrameChange(
                this.target,
                (float) (this.transparency + (target.transparency - this.transparency) * amount));
    }
}
