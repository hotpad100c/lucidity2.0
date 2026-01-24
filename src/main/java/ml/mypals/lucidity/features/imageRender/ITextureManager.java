package ml.mypals.lucidity.features.imageRender;

import net.minecraft.resources.ResourceLocation;

public interface ITextureManager {
    default void lucidity$destroyAll(ResourceLocation id) {}
}
