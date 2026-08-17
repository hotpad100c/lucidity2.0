package ml.mypals.lucidity.mixin.compat.flashback;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.moulberry.flashback.keyframe.Keyframe;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import ml.mypals.lucidity.flashback.SelectiveRenderingKeyFrame;
import ml.mypals.lucidity.flashback.SelectiveRenderingKeyFrameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;


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
        JsonObject json = new JsonObject();
        json.addProperty("type", SelectiveRenderingKeyFrameType.ID);
        json.addProperty("transparency", selectiveRenderingKeyFrame.transparency);
        json.add("interpolation_type", context.serialize(selectiveRenderingKeyFrame.interpolationType()));
        cir.setReturnValue(json);
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
        if (!json.has("type") || !SelectiveRenderingKeyFrameType.ID.equals(json.get("type").getAsString())) {
            return;
        }

        float transparency = json.has("transparency") ? json.get("transparency").getAsFloat() : 0.0f;
        InterpolationType interpolationType = json.has("interpolation_type")
                ? context.deserialize(json.get("interpolation_type"), InterpolationType.class)
                : InterpolationType.getDefault();

        cir.setReturnValue(new SelectiveRenderingKeyFrame(transparency, interpolationType));
    }
}
