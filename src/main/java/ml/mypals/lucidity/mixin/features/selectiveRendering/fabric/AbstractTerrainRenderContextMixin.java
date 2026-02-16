package ml.mypals.lucidity.mixin.features.selectiveRendering.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
//? if <=1.21.4 {
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractBlockRenderContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
//?}
//? if = 1.21.5 {
/*import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
*///?}
//? if >1.21.5 {
/*
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
*///?}
import net.minecraft.client.renderer.RenderType;
//? if >1.21.4 {
/*import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
//? if <=1.21.4 {
@Mixin(value = AbstractBlockRenderContext.class, remap = false)
//?} else {

/*@Mixin(value = TerrainRenderContext.class, remap = false)
*///?}
public abstract class AbstractTerrainRenderContextMixin {
    //? if <=1.21.4 {
    @Final @Shadow(remap = false)
    protected BlockRenderInfo blockInfo;
    @Shadow protected abstract VertexConsumer getVertexConsumer(RenderType renderType);
    //?}

    //? if <=1.21.4 {

    //? if <=1.21.3 {
    @Inject(method = "renderQuad", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractBlockRenderContext;bufferQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    //?} else {
    /*@Inject(method = "bufferQuad", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractBlockRenderContext;bufferQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    *///?}
    private void onBufferQuad(MutableQuadViewImpl quad, CallbackInfo ci) {
        if(!SelectiveRenderingManager.shouldRenderBlock(blockInfo.blockState,blockInfo.blockPos) && !SelectiveRenderingConfigs.isBlockFullyHidden()){
            for (int i = 0; i < 4; i++) {
                quad.color(i, rewriteQuadAlpha(quad.color(i), SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue()));
            }
        }
    }
    //? if <=1.21.3 {
    @WrapOperation(method = "renderQuad", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractBlockRenderContext;bufferQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    //?} else {
    /*@WrapOperation(method = "bufferQuad", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractBlockRenderContext;bufferQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    *///?}
    private void onBufferQuad(AbstractBlockRenderContext instance, MutableQuadViewImpl mutableQuadView, VertexConsumer vertexConsumer, Operation<Void> original) {
        if(!SelectiveRenderingManager.shouldRenderBlock(blockInfo.blockState,blockInfo.blockPos) && !SelectiveRenderingConfigs.isBlockFullyHidden()){
            original.call(instance,mutableQuadView,getVertexConsumer(RenderType.translucent()));
        }else{
            original.call(instance,mutableQuadView,vertexConsumer);
        }
    }
    //?} else {
    /*@WrapOperation(method = "bufferModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockStateModel;emitQuads(Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;Ljava/util/function/Predicate;)V"))
    private void onBufferQuad(BlockStateModel instance, QuadEmitter quadEmitter, BlockAndTintGetter blockAndTintGetter, BlockPos pos, BlockState state, RandomSource randomSource, Predicate predicate, Operation<Void> original) {
        if(!SelectiveRenderingManager.shouldRenderBlock(state,pos) && !SelectiveRenderingConfigs.isBlockFullyHidden()){
            original.call(instance, new QuadEmitter() {
                private final QuadEmitter base = quadEmitter;

                @Override
                public QuadEmitter pos(int i, float x, float y, float z) {
                    base.pos(i, x, y, z);
                    return this;
                }

                @Override
                public QuadEmitter color(int i, int color) {
                    base.color(i, rewriteQuadAlpha(
                            color,
                            SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue()
                    ));
                    return this;
                }

                @Override
                public QuadEmitter uv(int i, float u, float v) {
                    base.uv(i, u, v);
                    return this;
                }

                @Override
                public QuadEmitter spriteBake(TextureAtlasSprite sprite, int flags) {
                    base.spriteBake(sprite, flags);
                    return this;
                }

                @Override
                public QuadEmitter lightmap(int i, int lightmap) {
                    base.lightmap(i, lightmap);
                    return this;
                }

                @Override
                public QuadEmitter normal(int i, float x, float y, float z) {
                    base.normal(i, x, y, z);
                    return this;
                }

                @Override
                public QuadEmitter cullFace(@Nullable Direction direction) {
                    base.cullFace(direction);
                    return this;
                }

                @Override
                public QuadEmitter nominalFace(@Nullable Direction direction) {
                    base.nominalFace(direction);
                    return this;
                }

                @Override
                public QuadEmitter tintIndex(int i) {
                    base.tintIndex(i);
                    return this;
                }

                @Override
                public QuadEmitter tag(int i) {
                    base.tag(i);
                    return this;
                }

                @Override
                public QuadEmitter copyFrom(QuadView quadView) {
                    base.copyFrom(quadView);return this;}

                @Override
                public QuadEmitter fromVanilla(int[] data, int spriteIndex) {
                    base.fromVanilla(data, spriteIndex);
                    return this;
                }

                @Override
                public void pushTransform(QuadTransform transform) {
                    base.pushTransform(transform);
                }

                @Override
                public void popTransform() {
                    base.popTransform();
                }

                @Override
                public QuadEmitter emit() {
                    base.emit();
                    return this;
                }
                @Override public float x(int i) { return base.x(i); }
                @Override public float y(int i) { return base.y(i); }
                @Override public float z(int i) { return base.z(i); }
                @Override public float posByIndex(int v, int axis) { return base.posByIndex(v, axis); }
                @Override public Vector3f copyPos(int i, @Nullable Vector3f dst) { return base.copyPos(i, dst); }
                @Override public int color(int i) { return base.color(i); }
                @Override public float u(int i) { return base.u(i); }
                @Override public float v(int i) { return base.v(i); }
                @Override public Vector2f copyUv(int i, @Nullable Vector2f dst) { return base.copyUv(i, dst); }
                @Override public int lightmap(int i) { return base.lightmap(i); }
                @Override public boolean hasNormal(int i) { return base.hasNormal(i); }
                @Override public float normalX(int i) { return base.normalX(i); }
                @Override public float normalY(int i) { return base.normalY(i); }
                @Override public float normalZ(int i) { return base.normalZ(i); }
                @Override public @Nullable Vector3f copyNormal(int i, @Nullable Vector3f dst) { return base.copyNormal(i, dst); }
                @Override public @Nullable Direction cullFace() { return base.cullFace(); }


                @Override public int tintIndex() { return base.tintIndex(); }
                @Override public int tag() { return base.tag(); }
                @Override
                public void toVanilla(int[] data, int i) {
                    base.toVanilla(data, i);
                }
                @Override public @NotNull Direction lightFace() { return base.lightFace(); }
                @Override public @Nullable Direction nominalFace() { return base.nominalFace(); }
                @Override public Vector3fc faceNormal() { return base.faceNormal(); }
                //? if <=1.21.5 {
                @Override public RenderMaterial material() { return base.material(); }

                @Override
                public QuadEmitter material(RenderMaterial material) {
                    base.material(material);
                    return this;
                }

                @Override
                public QuadEmitter fromVanilla(BakedQuad quad, RenderMaterial material, @Nullable Direction dir) {
                    base.fromVanilla(quad, material, dir);
                    return this;
                }
                //?} else {

                /^

                @Override
                public @Nullable ChunkSectionLayer renderLayer() {
                    return base.renderLayer();
                }

                @Override
                public boolean emissive() {
                    return base.emissive();
                }

                @Override
                public boolean diffuseShade() {
                    return base.diffuseShade();
                }

                @Override
                public TriState ambientOcclusion() {
                    return base.ambientOcclusion();
                }

                @Override
                public ItemStackRenderState.@Nullable FoilType glint() {
                    return base.glint();
                }

                @Override
                public ShadeMode shadeMode() {
                    return base.shadeMode();
                }

                @Override
                public QuadEmitter renderLayer(@Nullable ChunkSectionLayer chunkSectionLayer) {
                    base.renderLayer(ChunkSectionLayer.TRANSLUCENT);
                    return this;
                }

                @Override
                public QuadEmitter fromBakedQuad(BakedQuad bakedQuad) {
                    base.fromBakedQuad(bakedQuad);
                    return this;
                }

                @Override
                public QuadEmitter emissive(boolean b) {
                    base.emissive(b);
                    return this;
                }

                @Override
                public QuadEmitter diffuseShade(boolean b) {
                    base.diffuseShade(b);
                    return this;
                }

                @Override
                public QuadEmitter ambientOcclusion(TriState triState) {
                    base.ambientOcclusion(triState);
                    return this;
                }

                @Override
                public QuadEmitter glint(ItemStackRenderState.@Nullable FoilType foilType) {
                    base.glint(foilType);
                    return this;
                }

                @Override
                public QuadEmitter shadeMode(ShadeMode shadeMode) {
                    base.shadeMode(shadeMode);
                    return this;
                }
                ^///?}
                //OOOHAG AHHAAAAAAAAAAAAAAAAAAAAa
            }, blockAndTintGetter, pos, state, randomSource, predicate);
        }else{
            original.call(instance, quadEmitter, blockAndTintGetter, pos, state, randomSource, predicate);
        }
    }
    *///?}

    @Unique
    private int rewriteQuadAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }
}
