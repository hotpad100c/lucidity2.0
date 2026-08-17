package ml.mypals.lucidity.flashback;

import com.google.common.collect.Maps;
import com.moulberry.flashback.keyframe.Keyframe;
import com.moulberry.flashback.keyframe.KeyframeType;
import com.moulberry.flashback.keyframe.change.KeyframeChange;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import com.moulberry.flashback.spline.CatmullRom;
import com.moulberry.flashback.spline.Hermite;
import imgui.moulberry90.ImGui;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 把 SelectiveRendering 的隐藏透明度接入 Flashback 关键帧。
 * 数值与 HIDDEN_BLOCK_TRANSPARENCY 同量纲（0..255），用 float 保存以便插值。
 */
public class SelectiveRenderingKeyFrame extends Keyframe {

    public float transparency;

    public SelectiveRenderingKeyFrame(float transparency) {
        this.transparency = transparency;
    }

    public SelectiveRenderingKeyFrame(float transparency, InterpolationType interpolationType) {
        this.transparency = transparency;
        this.interpolationType(interpolationType);
    }

    @Override
    public KeyframeType<?> keyframeType() {
        return SelectiveRenderingKeyFrameType.INSTANCE;
    }

    @Override
    public Keyframe copy() {
        return new SelectiveRenderingKeyFrame(this.transparency, this.interpolationType());
    }

    @Override
    public void renderEditKeyframe(Consumer<Consumer<Keyframe>> update) {
        float[] input = new float[]{ this.transparency };
        ImGui.setNextItemWidth(160);
        if (ImGui.sliderFloat(I18n.get("lucidity.flashback.hidden_transparency"), input, 0.0f, 255.0f, "%.0f")) {
            update.accept(keyframe -> ((SelectiveRenderingKeyFrame) keyframe).transparency = input[0]);
        }
    }

    @Override
    public @Nullable KeyframeChange createChange() {
        return new SelectiveRenderingKeyFrameChange(this.transparency);
    }

    /**
     * 参数排布与 Flashback 内置关键帧一致：this 是 p0，后三个是 p1/p2/p3，
     * 四个 float 是它们各自的时间，最后一个是 [0,1] 的插值量。
     * CatmullRom.value 期望的是相对 t0 的时间，所以先减掉 t0。
     */
    @Override
    public @Nullable KeyframeChange createSmoothInterpolatedChange(Keyframe p1, Keyframe p2, Keyframe p3,
                                                                   float t0, float t1, float t2, float t3,
                                                                   float amount) {
        float value = CatmullRom.value(
                this.transparency,
                ((SelectiveRenderingKeyFrame) p1).transparency,
                ((SelectiveRenderingKeyFrame) p2).transparency,
                ((SelectiveRenderingKeyFrame) p3).transparency,
                t1 - t0, t2 - t0, t3 - t0,
                amount);
        return new SelectiveRenderingKeyFrameChange(value);
    }

    @Override
    public @Nullable KeyframeChange createHermiteInterpolatedChange(Map<Float, Keyframe> keyframes, float amount) {
        double value = Hermite.value(
                Maps.transformValues(keyframes, keyframe -> (double) ((SelectiveRenderingKeyFrame) keyframe).transparency),
                amount);
        return new SelectiveRenderingKeyFrameChange((float) value);
    }
}
