package ml.mypals.lucidity.flashback;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * 关键帧的 JSON 读写。
 *
 * <p>为什么需要一个"具体类"的适配器，而不是只往 {@code Keyframe.TypeAdapter} 里注入：
 * Flashback 有好几处（比如 {@code EditorSceneHistoryAction.SetKeyframe}）是用单参的
 * {@code context.serialize(keyframe)} 写关键帧的，Gson 这时按<b>运行时类型</b>挑适配器。
 * Flashback 自家每个关键帧类都在 FlashbackGson 里注册了各自的适配器（各自负责写 "type"），
 * 我们的类不注册的话 Gson 就退回反射，字段照倒但没有 "type"，读回来时
 * {@code Keyframe.TypeAdapter} 认不出类型直接抛异常。
 *
 * <p>所以这里既是注册给 Gson 的适配器，也是 {@code KeyframeTypeAdapterMixin} 那条
 * 按 {@code Keyframe.class} 序列化的路径共用的实现，两边写出来的 JSON 完全一致。
 */
public final class SelectiveRenderingKeyFrameSerializer
        implements JsonSerializer<SelectiveRenderingKeyFrame>, JsonDeserializer<SelectiveRenderingKeyFrame> {

    public static final SelectiveRenderingKeyFrameSerializer INSTANCE = new SelectiveRenderingKeyFrameSerializer();

    private SelectiveRenderingKeyFrameSerializer() {}

    public static JsonObject write(SelectiveRenderingKeyFrame keyframe, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("type", SelectiveRenderingKeyFrameType.ID);
        json.addProperty("transparency", keyframe.transparency);
        // 目标是选区的对角线（x1,y1,z1:x2,y2,z2），空串表示全局，工程文件里一眼能看出指的是哪块
        json.addProperty("target", keyframe.target);
        json.add("interpolation_type", context.serialize(keyframe.interpolationType()));
        return json;
    }

    public static SelectiveRenderingKeyFrame read(JsonObject json, JsonDeserializationContext context) {
        float transparency = json.has("transparency") ? json.get("transparency").getAsFloat() : 0.0f;

        // 这个字段是后加的，旧工程里的关键帧一律按"改全局"处理
        String target = json.has("target")
                ? json.get("target").getAsString()
                : SelectiveRenderingKeyFrame.GLOBAL_TARGET;

        InterpolationType interpolationType = json.has("interpolation_type")
                ? context.deserialize(json.get("interpolation_type"), InterpolationType.class)
                : InterpolationType.getDefault();

        return new SelectiveRenderingKeyFrame(target, transparency, interpolationType);
    }

    /**
     * 抢救 0.x 那阵写坏的关键帧。
     *
     * <p>在给 Gson 注册具体类适配器之前，Flashback 那几处单参 {@code context.serialize} 会
     * 把我们的关键帧按反射倒成字段原样 —— 没有 "type"，而且用的是字段名
     * {@code interpolationType}（我们自己写出来的是 {@code interpolation_type}）。
     * 这种 JSON 现在读回来会让整个 EditorState 加载失败，所以按这两个特征认一下，
     * 认出来就当自己的关键帧读。认不出返回 null，交回 Flashback 原本的报错逻辑。
     *
     * <p>只对着已经存在的坏文件，新写出来的都带 "type"；哪天不用管旧文件了可以整段删掉。
     */
    @Nullable
    public static SelectiveRenderingKeyFrame readLegacyReflective(JsonObject json, JsonDeserializationContext context) {
        if (json.has("type") || !json.has("transparency") || !json.has("interpolationType")) {
            return null;
        }
        try {
            float transparency = json.get("transparency").getAsFloat();
            String target = json.has("target")
                    ? json.get("target").getAsString()
                    : SelectiveRenderingKeyFrame.GLOBAL_TARGET;
            InterpolationType interpolationType =
                    context.deserialize(json.get("interpolationType"), InterpolationType.class);
            return new SelectiveRenderingKeyFrame(target, transparency,
                    interpolationType == null ? InterpolationType.getDefault() : interpolationType);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public JsonElement serialize(SelectiveRenderingKeyFrame keyframe, Type type, JsonSerializationContext context) {
        return write(keyframe, context);
    }

    @Override
    public SelectiveRenderingKeyFrame deserialize(JsonElement element, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        return read(element.getAsJsonObject(), context);
    }
}
