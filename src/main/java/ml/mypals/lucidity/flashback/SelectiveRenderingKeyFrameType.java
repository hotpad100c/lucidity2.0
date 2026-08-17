package ml.mypals.lucidity.flashback;

import com.moulberry.flashback.editor.ui.ReplayUI;
import com.moulberry.flashback.keyframe.KeyframeType;
import com.moulberry.flashback.keyframe.change.KeyframeChange;
import com.moulberry.flashback.keyframe.handler.KeyframeHandler;
import com.moulberry.flashback.keyframe.handler.MinecraftKeyframeHandler;
import imgui.moulberry90.ImGui;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import net.minecraft.client.resources.language.I18n;

public class SelectiveRenderingKeyFrameType implements KeyframeType<SelectiveRenderingKeyFrame> {

    /** 会被写进回放工程文件，改动它等于让旧工程里的关键帧读不出来。 */
    public static final String ID = "lucidity_hidden_transparency";

    public static final SelectiveRenderingKeyFrameType INSTANCE = new SelectiveRenderingKeyFrameType();

    private SelectiveRenderingKeyFrameType() {}

    @Override
    public Class<? extends KeyframeChange> keyframeChangeType() {
        return SelectiveRenderingKeyFrameChange.class;
    }

    /**
     * MinecraftKeyframeHandler.supportedChanges 是硬编码的不可变 Set.of(...)，
     * KeyframeRegistry.register 不会往里添加，第三方 KeyframeChange 永远不在其中。
     * 好在 EditorState.applyKeyframes 和 TimelineWindow 调的都是这个 default 方法，
     * 覆写它就能绕过白名单，不需要 mixin。
     *
     * 限定为 MinecraftKeyframeHandler：这是客户端视觉用的那个 handler。
     * ReplayServerKeyframeHandler（服务端线程）和 CameraPath 的采样 handler 都不该收到这个变更。
     */
    @Override
    public boolean supportsHandler(KeyframeHandler keyframeHandler) {
        return keyframeHandler instanceof MinecraftKeyframeHandler;
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

    @Override
    public SelectiveRenderingKeyFrame createDirect() {
        return new SelectiveRenderingKeyFrame(SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue());
    }

    @Override
    public KeyframeCreatePopup<SelectiveRenderingKeyFrame> createPopup() {
        float[] input = new float[]{ SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue() };
        return () -> {
            ImGui.sliderFloat(I18n.get("lucidity.flashback.hidden_transparency"), input, 0.0f, 255.0f);
            if (ImGui.button(I18n.get("flashback.add")) || ReplayUI.consumeConfirm()) {
                return new SelectiveRenderingKeyFrame(input[0]);
            }
            ImGui.sameLine();
            if (ImGui.button(I18n.get("gui.cancel")) || ReplayUI.consumeCancel()) {
                ImGui.closeCurrentPopup();
            }
            return null;
        };
    }
}
