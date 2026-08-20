package ml.mypals.lucidity.features.selectiveRendering;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class SelectiveRenderingRenderTypes {

    private static final Map<RenderType, Map<Integer, RenderType>> HIDDEN_VARIANTS = new ConcurrentHashMap<>();
    private static final Map<RenderType, Integer> HIDDEN = new ConcurrentHashMap<>();

    private SelectiveRenderingRenderTypes() {}

    @Nullable
    public static Identifier textureOf(RenderType renderType) {
        for (RenderSetup.TextureBinding binding : renderType.state.textures.values()) {
            return binding.location();
        }
        return null;
    }

    public static RenderType hiddenVariantOf(RenderType original) {
        return hiddenVariantOf(original, SelectiveRenderingManager.GLOBAL_TRANSPARENCY_SOURCE);
    }

    public static RenderType hiddenVariantOf(RenderType original, int transparencySource) {
        return HIDDEN_VARIANTS
                .computeIfAbsent(original, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(transparencySource, source -> {
                    Identifier texture = textureOf(original);
                    RenderType translucent = texture != null
                            ? RenderTypes.entityTranslucent(texture)
                            : RenderTypes.translucentMovingBlock();
                    RenderType variant = RenderType.create(
                            "lucidity_hidden/" + (texture != null ? texture : "moving_block") + "/" + source,
                            translucent.state);
                    HIDDEN.put(variant, source);
                    return variant;
                });
    }

    public static boolean isHiddenVariant(RenderType renderType) {
        return HIDDEN.containsKey(renderType);
    }
    @Nullable
    public static Integer transparencySourceOf(RenderType renderType) {
        return HIDDEN.get(renderType);
    }
}
