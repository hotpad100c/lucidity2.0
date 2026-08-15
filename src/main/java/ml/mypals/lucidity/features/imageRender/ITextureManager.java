package ml.mypals.lucidity.features.imageRender;

import net.minecraft.resources.Identifier;

public interface ITextureManager {
    default void lucidity$destroyAll(Identifier id) {}
}
