package ml.mypals.lucidity.features.selectiveRendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.SequencedMap;

public class ControllableTransparentBuffersWrapper extends MultiBufferSource.BufferSource{
    private final MultiBufferSource.BufferSource multiBufferSource;
    public ControllableTransparentBuffersWrapper(MultiBufferSource.BufferSource multiBufferSource) {
        super(multiBufferSource.sharedBuffer, multiBufferSource.fixedBuffers);
        this.multiBufferSource = multiBufferSource;
    }
    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
        RenderType real = unwrapRenderType(renderType);

        if (real instanceof RenderType.CompositeRenderType composite) {
            if (composite.state.textureState instanceof RenderStateShard.TextureStateShard tex
                    && tex.texture.isPresent()) {

                return new ControllableTransparentVertexConsumer(
                        multiBufferSource.getBuffer(
                                RenderType.entityTranslucent(tex.texture.get())
                        )
                );
            }
        }
        return new ControllableTransparentVertexConsumer(multiBufferSource.getBuffer(RenderType.translucent()));
    }
    public void endLastBatch() {
        multiBufferSource.endLastBatch();
    }

    public void endBatch() {
       multiBufferSource.endBatch();
    }

    public void endBatch(@NotNull RenderType renderType) {
        multiBufferSource.endBatch(renderType);
    }

    //IRIS support
    public static RenderType unwrapRenderType(RenderType rt) {
        try {
            Class<?> c = rt.getClass();

            while (c != RenderType.CompositeRenderType.class) {
                for (Field f : c.getDeclaredFields()) {
                    if (RenderType.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Object inner = f.get(rt);
                        if (inner instanceof RenderType innerRt) {
                            rt = innerRt;
                            c = rt.getClass();
                            continue;
                        }
                    }
                }
                break;
            }
        } catch (Throwable ignored) {}

        return rt;
    }

}
