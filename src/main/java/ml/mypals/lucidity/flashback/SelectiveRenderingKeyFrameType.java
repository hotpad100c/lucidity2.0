package ml.mypals.lucidity.flashback;

import com.moulberry.flashback.editor.ui.ReplayUI;
import com.moulberry.flashback.keyframe.KeyframeType;
import com.moulberry.flashback.keyframe.change.KeyframeChange;
import com.moulberry.flashback.keyframe.handler.KeyframeHandler;
import com.moulberry.flashback.keyframe.handler.MinecraftKeyframeHandler;
import imgui.moulberry90.ImGui;
import imgui.moulberry90.type.ImInt;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import net.minecraft.client.resources.language.I18n;

import java.util.List;

public class SelectiveRenderingKeyFrameType implements KeyframeType<SelectiveRenderingKeyFrame> {

    /** 会被写进回放工程文件，改动它等于让旧工程里的关键帧读不出来。 */
    public static final String ID = "lucidity_hidden_transparency";

    public static final SelectiveRenderingKeyFrameType INSTANCE = new SelectiveRenderingKeyFrameType();

    private SelectiveRenderingKeyFrameType() {}

    @Override
    public Class<? extends KeyframeChange> keyframeChangeType() {
        return SelectiveRenderingKeyFrameChange.class;
    }

    @Override
    public boolean supportsHandler(KeyframeHandler keyframeHandler) {
        return keyframeHandler instanceof MinecraftKeyframeHandler;
    }
    @Override
    public boolean allowApplyingDuplicateKeyframeChanges() {
        return true;
    }

    @Override
    public String icon() {
        return "T";
    }

    @Override
    public String name() {
        return I18n.get("lucidity.flashback.keyframe.hidden_transparency");
    }

    @Override
    public String id() {
        return ID;
    }

    /** 不弹窗直接添加时给全局透明度记一帧；要针对选区就走 {@link #createPopup()}。 */
    @Override
    public SelectiveRenderingKeyFrame createDirect() {
        return new SelectiveRenderingKeyFrame(
                SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue());
    }

    @Override
    public KeyframeCreatePopup<SelectiveRenderingKeyFrame> createPopup() {
        List<String> options = SelectiveRenderingKeyFrame.targetOptions(SelectiveRenderingKeyFrame.GLOBAL_TARGET);
        String[] labels = SelectiveRenderingKeyFrame.targetLabels(options);
        ImInt selected = new ImInt(0);
        float[] input = new float[]{ SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue() };

        return () -> {
            ImGui.combo(I18n.get("lucidity.flashback.target"), selected, labels);
            ImGui.sliderFloat(I18n.get("lucidity.flashback.hidden_transparency"), input, 0.0f, 255.0f);

            if (ImGui.button(I18n.get("flashback.add")) || ReplayUI.consumeConfirm()) {
                String target = options.get(Math.clamp(selected.get(), 0, options.size() - 1));
                return new SelectiveRenderingKeyFrame(target, input[0]);
            }
            ImGui.sameLine();
            if (ImGui.button(I18n.get("gui.cancel")) || ReplayUI.consumeCancel()) {
                ImGui.closeCurrentPopup();
            }
            return null;
        };
    }
}
