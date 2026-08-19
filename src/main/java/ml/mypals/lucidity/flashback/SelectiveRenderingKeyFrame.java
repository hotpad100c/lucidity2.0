package ml.mypals.lucidity.flashback;

import com.moulberry.flashback.keyframe.Keyframe;
import com.moulberry.flashback.keyframe.KeyframeType;
import com.moulberry.flashback.keyframe.change.KeyframeChange;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import com.moulberry.flashback.spline.CatmullRom;
import com.moulberry.flashback.spline.Hermite;
import imgui.moulberry90.ImGui;
import imgui.moulberry90.type.ImInt;
import ml.mypals.lucidity.features.selectiveRendering.AreaBox;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * 把 SelectiveRendering 的隐藏透明度接入 Flashback 关键帧。
 * 数值与 HIDDEN_BLOCK_TRANSPARENCY 同量纲（0..255），用 float 保存以便插值。
 *
 * <p>一个关键帧只管一个目标：要么是全局透明度，要么是某一个选区自己的透明度。想同时
 * 动几个选区就开几条轨道，各管各的。选区用它的对角线（{@link AreaBox#getKey()}）点名 ——
 * 列表下标会随着增删选区整体错位，对角只要选区本身不动就一直有效。
 */
public class SelectiveRenderingKeyFrame extends Keyframe {

    /** {@link #target} 取这个值时，这个关键帧改的是全局透明度。 */
    public static final String GLOBAL_TARGET = "";

    /** {@link #GLOBAL_TARGET} 或某个选区的对角线。 */
    public String target;
    public float transparency;

    public SelectiveRenderingKeyFrame(float transparency) {
        this(GLOBAL_TARGET, transparency);
    }

    public SelectiveRenderingKeyFrame(String target, float transparency) {
        this.target = target == null ? GLOBAL_TARGET : target;
        this.transparency = transparency;
    }

    public SelectiveRenderingKeyFrame(String target, float transparency, InterpolationType interpolationType) {
        this(target, transparency);
        this.interpolationType(interpolationType);
    }

    @Override
    public KeyframeType<?> keyframeType() {
        return SelectiveRenderingKeyFrameType.INSTANCE;
    }

    @Override
    public Keyframe copy() {
        return new SelectiveRenderingKeyFrame(this.target, this.transparency, this.interpolationType());
    }

    // ------------------------------------------------------------------
    // 目标下拉框：创建弹窗和编辑面板共用
    // ------------------------------------------------------------------

    /** 下拉框的选项：全局 + 当前所有选区，外加 current 本身（选区可能已经没了）。 */
    public static List<String> targetOptions(String current) {
        List<String> options = new ArrayList<>();
        options.add(GLOBAL_TARGET);
        for (AreaBox area : SelectiveRenderingManager.selectedAreas) {
            options.add(area.getKey());
        }
        if (!options.contains(current)) {
            options.add(current);
        }
        return options;
    }

    public static String[] targetLabels(List<String> options) {
        String[] labels = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            String target = options.get(i);
            if (GLOBAL_TARGET.equals(target)) {
                labels[i] = I18n.get("lucidity.flashback.target.global");
            } else {
                labels[i] = I18n.get(SelectiveRenderingManager.findArea(target) != null
                        ? "lucidity.flashback.target.area"
                        : "lucidity.flashback.target.area_missing", target);
            }
        }
        return labels;
    }

    @Override
    public void renderEditKeyframe(Consumer<Consumer<Keyframe>> update) {
        List<String> options = targetOptions(this.target);
        ImInt selected = new ImInt(options.indexOf(this.target));
        ImGui.setNextItemWidth(160);
        if (ImGui.combo(I18n.get("lucidity.flashback.target"), selected, targetLabels(options))) {
            String chosen = options.get(Math.clamp(selected.get(), 0, options.size() - 1));
            update.accept(keyframe -> ((SelectiveRenderingKeyFrame) keyframe).target = chosen);
        }

        float[] input = new float[]{ this.transparency };
        ImGui.setNextItemWidth(160);
        if (ImGui.sliderFloat(I18n.get("lucidity.flashback.hidden_transparency"), input, 0.0f, 255.0f, "%.0f")) {
            update.accept(keyframe -> ((SelectiveRenderingKeyFrame) keyframe).transparency = input[0]);
        }
    }

    @Override
    public @Nullable KeyframeChange createChange() {
        return new SelectiveRenderingKeyFrameChange(this.target, this.transparency);
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
        SelectiveRenderingKeyFrame f1 = (SelectiveRenderingKeyFrame) p1;
        SelectiveRenderingKeyFrame f2 = (SelectiveRenderingKeyFrame) p2;
        SelectiveRenderingKeyFrame f3 = (SelectiveRenderingKeyFrame) p3;

        // 一条轨道上混了不同目标时插不了值：这几个数字根本不是一样东西的读数。
        // 退回本帧的值，等目标重新一致再恢复平滑。
        if (!this.target.equals(f1.target) || !this.target.equals(f2.target) || !this.target.equals(f3.target)) {
            return createChange();
        }

        float value = CatmullRom.value(
                this.transparency, f1.transparency, f2.transparency, f3.transparency,
                t1 - t0, t2 - t0, t3 - t0,
                amount);
        return new SelectiveRenderingKeyFrameChange(this.target, value);
    }

    @Override
    public @Nullable KeyframeChange createHermiteInterpolatedChange(Map<Float, Keyframe> keyframes, float amount) {
        // 同上：只拿目标一致的那些帧建样条
        TreeMap<Float, Double> samples = new TreeMap<>();
        for (Map.Entry<Float, Keyframe> entry : keyframes.entrySet()) {
            SelectiveRenderingKeyFrame frame = (SelectiveRenderingKeyFrame) entry.getValue();
            if (this.target.equals(frame.target)) {
                samples.put(entry.getKey(), (double) frame.transparency);
            }
        }
        if (samples.size() < 2) {
            return createChange();
        }
        return new SelectiveRenderingKeyFrameChange(this.target, (float) Hermite.value(samples, amount));
    }
}
