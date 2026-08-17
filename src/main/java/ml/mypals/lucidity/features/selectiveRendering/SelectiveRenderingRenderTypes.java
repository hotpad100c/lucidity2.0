package ml.mypals.lucidity.features.selectiveRendering;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1.21.11 起 RenderType 是私有构造的具体类，没法再像以前那样包一层当标记用。
 *
 * 这里改成给每个需要隐藏的 RenderType 造一个<b>专属实例</b>：它复用半透明 RenderType
 * 的 RenderSetup（管线、纹理、混合状态都一样），但作为一个独立对象存在，
 * 因此在 BufferSource 里会拿到自己的批次，不会和真正的半透明几何混在一起。
 *
 * 拿到独立批次之后，透明度就不必再烘焙进顶点色了 —— RenderType.draw 每批只写一次
 * colorModulator，{@code RenderTypeDrawMixin} 认出这些实例后把 alpha 换掉即可。
 * 这样改透明度不需要重建任何网格。
 *
 * RenderType 没有覆写 equals/hashCode，所以 ConcurrentHashMap 天然就是按实例身份索引的。
 */
public final class SelectiveRenderingRenderTypes {

    private static final Map<RenderType, RenderType> HIDDEN_VARIANTS = new ConcurrentHashMap<>();
    private static final Map<RenderType, Boolean> HIDDEN = new ConcurrentHashMap<>();

    private SelectiveRenderingRenderTypes() {}

    /** 取出这个 RenderType 绑定的纹理；取不到返回 null。 */
    @Nullable
    public static Identifier textureOf(RenderType renderType) {
        for (RenderSetup.TextureBinding binding : renderType.state.textures.values()) {
            if (binding.location() != null) {
                return binding.location();
            }
        }
        return null;
    }

    /** 返回与 original 对应的、专供隐藏几何使用的独立 RenderType 实例。 */
    public static RenderType hiddenVariantOf(RenderType original) {
        return HIDDEN_VARIANTS.computeIfAbsent(original, key -> {
            Identifier texture = textureOf(key);
            RenderType translucent = texture != null
                    ? RenderTypes.entityTranslucent(texture)
                    : RenderTypes.translucentMovingBlock();
            // 复用半透明的 RenderSetup，但作为独立实例，这样才能单独绑 uniform
            RenderType variant = RenderType.create(
                    "lucidity_hidden/" + (texture != null ? texture : "moving_block"),
                    translucent.state);
            HIDDEN.put(variant, Boolean.TRUE);
            return variant;
        });
    }

    /** 这个 RenderType 是不是我们造出来的隐藏几何批次。 */
    public static boolean isHiddenVariant(RenderType renderType) {
        return HIDDEN.containsKey(renderType);
    }
}
