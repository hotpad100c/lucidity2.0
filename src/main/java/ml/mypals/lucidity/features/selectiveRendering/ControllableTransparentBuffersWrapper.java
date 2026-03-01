package ml.mypals.lucidity.features.selectiveRendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.mixin.features.selectiveRendering.accessor.BufferSourceAccessor;
import ml.mypals.lucidity.mixin.features.selectiveRendering.accessor.CompositeStateAccessor;
import ml.mypals.lucidity.mixin.features.selectiveRendering.accessor.EmptyTextureStateShardAccessor;
import ml.mypals.lucidity.mixin.features.selectiveRendering.accessor.RenderStateAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class ControllableTransparentBuffersWrapper extends MultiBufferSource.BufferSource{
    private final MultiBufferSource.BufferSource multiBufferSource;
    public ControllableTransparentBuffersWrapper(MultiBufferSource.BufferSource source) {
        super(
                ((BufferSourceAccessor) source).lucidity$getSharedBuffer(),
                ((BufferSourceAccessor) source).lucidity$getFixedBuffers()
        );
        this.multiBufferSource = source;
    }
    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
        RenderType real = unwrapRenderType(renderType);

        //? if >=1.21.9 {
        /*if(real instanceof SelectiveRenderingRenderTypeWrapper selectiveRenderingRenderTypeWrapper
                && selectiveRenderingRenderTypeWrapper.base != null){
            RenderType renderType1 = selectiveRenderingRenderTypeWrapper.base;
            return getTransparentBuffer(renderType1);
        }
        return multiBufferSource.getBuffer(renderType);
        *///?} else {
        return getTransparentBuffer(real);
        //?}
    }
    public @NotNull VertexConsumer getTransparentBuffer(@NotNull RenderType renderType) {
        if ((renderType.format() == DefaultVertexFormat.NEW_ENTITY) && renderType instanceof RenderType.CompositeRenderType composite) {
            RenderStateAccessor rt = (RenderStateAccessor) renderType;

            RenderType.CompositeState compositeState = rt.getState();

            RenderStateShard.EmptyTextureStateShard textureState = ((CompositeStateAccessor)(Object) compositeState).getTextureState();
            if (textureState instanceof RenderStateShard.TextureStateShard tex
                    && ((EmptyTextureStateShardAccessor)tex).getTexture().isPresent()) {
                return new ControllableTransparentVertexConsumer(
                    multiBufferSource.getBuffer(
                        RenderType.entityTranslucent(((EmptyTextureStateShardAccessor)tex).getTexture().get())
                    )
                );
            }
        }else {
            return new ControllableTransparentVertexConsumer(multiBufferSource.getBuffer(renderType));
        }
        return new ControllableTransparentVertexConsumer(multiBufferSource.getBuffer(RenderType.translucentMovingBlock()));

    }

    public void endBatch() {
       multiBufferSource.endBatch();
    }

    public void endBatch(@NotNull RenderType renderType) {
        multiBufferSource.endBatch(renderType);
    }

    //IRIS support
    public static RenderType unwrapRenderType(RenderType rt) {

        //? if >=1.21.9 {
        /*if(rt instanceof SelectiveRenderingRenderTypeWrapper){
            return rt;
        }
        *///?}

        try {
            Class<?> c = rt.getClass();

            while (c != RenderType.CompositeRenderType.class) {
                for (Field f : c.getDeclaredFields()) {
                    if (RenderType.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Object inner = f.get(rt);
                        //? if >=1.21.9 {
                        /*if(inner instanceof SelectiveRenderingRenderTypeWrapper selectiveRenderingRenderTypeWrapper){
                            return selectiveRenderingRenderTypeWrapper;
                        }
                        *///?}
                        if (inner instanceof RenderType innerRt) {
                            rt = innerRt;
                        }
                    }
                }
                break;
            }
        } catch (Throwable ignored) {}

        return rt;
    }

}
