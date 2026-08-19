package ml.mypals.lucidity.mixin.compat.flashback;

import com.google.gson.GsonBuilder;
import com.moulberry.flashback.FlashbackGson;
import ml.mypals.lucidity.flashback.SelectiveRenderingKeyFrame;
import ml.mypals.lucidity.flashback.SelectiveRenderingKeyFrameSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把我们的关键帧类注册进 Flashback 的 Gson。
 *
 * <p>Flashback 有几处是用单参 {@code context.serialize(keyframe)} 写关键帧的，Gson 那时
 * 按运行时类型挑适配器 —— 它自家每个关键帧类都在这里注册过，没注册的类会退回反射，
 * 写出来的 JSON 缺 "type"，读回来就炸（EditorState.load 直接抛
 * "Unable to determine type of keyframe"）。
 *
 * <p>{@code build()} 返回的是还没 create 的 GsonBuilder，PRETTY 和 COMPRESSED 各调一次，
 * 所以在它返回时补一条注册，两个实例都能拿到。
 */
@Pseudo
@Mixin(FlashbackGson.class)
public class FlashbackGsonMixin {

    @Inject(method = "build", at = @At("RETURN"), remap = false)
    private static void lucidity$registerKeyframeAdapter(CallbackInfoReturnable<GsonBuilder> cir) {
        GsonBuilder builder = cir.getReturnValue();
        if (builder != null) {
            builder.registerTypeAdapter(SelectiveRenderingKeyFrame.class, SelectiveRenderingKeyFrameSerializer.INSTANCE);
        }
    }
}
