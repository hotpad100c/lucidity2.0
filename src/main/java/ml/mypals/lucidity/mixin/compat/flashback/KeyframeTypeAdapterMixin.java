package ml.mypals.lucidity.mixin.compat.flashback;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.moulberry.flashback.keyframe.Keyframe;
import ml.mypals.lucidity.flashback.SelectiveRenderingKeyFrame;
import ml.mypals.lucidity.flashback.SelectiveRenderingKeyFrameSerializer;
import ml.mypals.lucidity.flashback.SelectiveRenderingKeyFrameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

/**
 * 按 {@code Keyframe.class} 显式读写关键帧时走的是这里；按运行时类型走的那条路
 * 由 {@link FlashbackGsonMixin} 注册的适配器接住。两边共用同一套读写实现，
 * 免得两处格式跑偏。
 */
@Pseudo
@Mixin(Keyframe.TypeAdapter.class)
public class KeyframeTypeAdapterMixin {

    @Inject(
            method = "serialize(Lcom/moulberry/flashback/keyframe/Keyframe;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void lucidity$serialize(Keyframe keyframe, Type type, JsonSerializationContext context,
                                    CallbackInfoReturnable<JsonElement> cir) {
        if (!(keyframe instanceof SelectiveRenderingKeyFrame selectiveRenderingKeyFrame)) {
            return;
        }
        cir.setReturnValue(SelectiveRenderingKeyFrameSerializer.write(selectiveRenderingKeyFrame, context));
    }

    @Inject(
            method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lcom/moulberry/flashback/keyframe/Keyframe;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void lucidity$deserialize(JsonElement element, Type type, JsonDeserializationContext context,
                                      CallbackInfoReturnable<Keyframe> cir) {
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject json = element.getAsJsonObject();
        if (!json.has("type")) {
            SelectiveRenderingKeyFrame recovered = SelectiveRenderingKeyFrameSerializer.readLegacyReflective(json, context);
            if (recovered != null) {
                cir.setReturnValue(recovered);
            }
            return;
        }
        if (!SelectiveRenderingKeyFrameType.ID.equals(json.get("type").getAsString())) {
            return;
        }

        cir.setReturnValue(SelectiveRenderingKeyFrameSerializer.read(json, context));
    }
}
