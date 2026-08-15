package ml.mypals.lucidity.mixin.features.imageRendeing;

import ml.mypals.lucidity.features.imageRender.ITextureManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin implements ITextureManager {
    @Shadow
    @Final
    private  Map<Identifier, AbstractTexture> byPath;

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract void release(Identifier resourceLocation);

    @Override
    public void lucidity$destroyAll(Identifier identifier) {
        String targetPath = identifier.getPath();
        List<Identifier> needsToRemove = new ArrayList<>();
        this.byPath.forEach(((identifier1, abstractTexture) -> {
            String path = identifier1.getPath();
            boolean bl = path.startsWith(targetPath);
            if(bl){
                LOGGER.info("Destroyed {}", identifier1.getPath());
                needsToRemove.add(identifier1);
            }
        }));
        needsToRemove.forEach(this::release);
    }

}
