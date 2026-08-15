package ml.mypals.lucidity.features.selectiveRendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.mixin.features.selectiveRendering.accessor.BufferSourceAccessor;
import ml.mypals.lucidity.mixin.features.selectiveRendering.accessor.CompositeStateAccessor;
import ml.mypals.lucidity.mixin.features.selectiveRendering.accessor.EmptyTextureStateShardAccessor;
import ml.mypals.lucidity.mixin.features.selectiveRendering.accessor.RenderStateAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
//? if <1.21.11 {
/*import net.minecraft.client.renderer.RenderStateShard;
*///?}
import net.minecraft.client.renderer.rendertype.*;

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

        //? if >=1.21.11 {
        // 隐藏几何在提交阶段就已经被换成了自己的 RenderType 实例，
        // 透明度由 RenderTypeDrawMixin 在绘制时通过 uniform 施加，这里不需要再包顶点消费者。
        return multiBufferSource.getBuffer(real);
        //?} else if >=1.21.9 {
        /*if(real instanceof SelectiveRenderingRenderTypeWrapper selectiveRenderingRenderTypeWrapper
                && selectiveRenderingRenderTypeWrapper.base != null){
            RenderType renderType1 = selectiveRenderingRenderTypeWrapper.base;
            return getTransparentBuffer(renderType1);
        }
        return multiBufferSource.getBuffer(renderType);
        *///?} else {
        /*return getTransparentBuffer(real);
        *///?}
    }
    public @NotNull VertexConsumer getTransparentBuffer(@NotNull RenderType renderType) {
        //? if >=1.21.11 {
        if (renderType.format() == DefaultVertexFormat.NEW_ENTITY) {
            return multiBufferSource.getBuffer(SelectiveRenderingRenderTypes.hiddenVariantOf(renderType));
        }
        return new ControllableTransparentVertexConsumer(multiBufferSource.getBuffer(renderType));
        //?} else {
        /*if ((renderType.format() == DefaultVertexFormat.NEW_ENTITY) && renderType instanceof RenderType.CompositeRenderType composite) {
            RenderStateAccessor rt = (RenderStateAccessor) renderType;

            RenderType.CompositeState compositeState = rt.getState();

            RenderStateShard.EmptyTextureStateShard textureState = ((CompositeStateAccessor)(Object) compositeState).getTextureState();
            if (textureState instanceof RenderStateShard.TextureStateShard tex
                    && ((EmptyTextureStateShardAccessor)tex).getTexture().isPresent()) {
                return new ControllableTransparentVertexConsumer(
                    multiBufferSource.getBuffer(
                        RenderTypes.entityTranslucent(((EmptyTextureStateShardAccessor)tex).getTexture().get())
                    )
                );
            }
        }else {
            return new ControllableTransparentVertexConsumer(multiBufferSource.getBuffer(renderType));
        }
        return new ControllableTransparentVertexConsumer(multiBufferSource.getBuffer(RenderTypes.translucentMovingBlock()));
        *///?}
    }

    public void endBatch() {
       multiBufferSource.endBatch();
    }

    public void endBatch(@NotNull RenderType renderType) {
        multiBufferSource.endBatch(renderType);
    }

    //IRIS support
    public static RenderType unwrapRenderType(RenderType rt) {

        //? if >=1.21.11 {
        if (SelectiveRenderingRenderTypes.isHiddenVariant(rt)) {
            return rt;
        }
        //?} else if >=1.21.9 {
        /*if(rt instanceof SelectiveRenderingRenderTypeWrapper){
            return rt;
        }
        *///?}

        try {
            Class<?> c = rt.getClass();

            //? if >=1.21.11 {
            // 1.21.11 起 RenderType 就是单一具体类，没有 CompositeRenderType 了
            while (c != RenderType.class) {
            //?} else {
            /*while (c != RenderType.CompositeRenderType.class) {
            *///?}
                for (Field f : c.getDeclaredFields()) {
                    if (RenderType.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Object inner = f.get(rt);
                        //? if >=1.21.11 {
                        if (inner instanceof RenderType innerType && SelectiveRenderingRenderTypes.isHiddenVariant(innerType)) {
                            return innerType;
                        }
                        //?} else if >=1.21.9 {
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
