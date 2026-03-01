package ml.mypals.lucidity.features.worldEaterHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//? if >=1.21.5 {
/*import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.resources.model.QuadCollection;
*///?} else {
import net.minecraft.client.resources.model.BakedModel;
import net.fabricmc.fabric.impl.renderer.VanillaModelEncoder;
import java.util.function.Supplier;
import java.util.ArrayList;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Predicate;

//? if >=1.21.5 {
/*public class ExtrudedBlockBakedModel implements BlockStateModel {
*///?} else {
public class ExtrudedBlockBakedModel implements BakedModel {
//?}

    //? if >=1.21.5 {
    /*private final BlockStateModel base;
    *///?} else {
    private final BakedModel base;
    //?}
    private final float height;
    private final Quaternionf rotation;

    //? if >=1.21.5 {
    /*public ExtrudedBlockBakedModel(BlockStateModel base, float height) {
    *///?} else {

    public ExtrudedBlockBakedModel(BakedModel base, float height) {
    //?}
        this.base = base;
        this.height = height;
        this.rotation = new Quaternionf()
                .rotateTo(
                        new Vector3f(1, 1, 1).normalize(),
                        new Vector3f(0, 1, 0)
                )
                .rotateY((float) Math.toRadians(45));
    }
    //? if >=1.21.5 {

    /*@Override
    public void collectParts(@NotNull RandomSource randomSource, @NotNull List<BlockModelPart> baseQuads) {
        base.collectParts(randomSource,baseQuads);
    }

    @Override
    public @NotNull TextureAtlasSprite particleIcon() {
        return base.particleIcon();
    }

    @Override
    public void emitQuads(
            QuadEmitter emitter,
            BlockAndTintGetter blockView,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            Predicate<@Nullable Direction> cullTest
    ) {
        base.emitQuads(emitter, blockView, pos, state, random, cullTest);

        BlockModelPart extraPart = buildExtraPart(state, random);

        extraPart.emitQuads(emitter, direction -> false);
    }
    private BlockModelPart buildExtraPart(BlockState state, RandomSource random) {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        BlockStateModel fresh = dispatcher.getBlockModel(state);

        List<BlockModelPart> freshParts = fresh.collectParts(random);

        QuadCollection.Builder extra = new QuadCollection.Builder();
        TextureAtlasSprite particle = null;

        for (BlockModelPart part : freshParts) {
            particle = part.particleIcon();

            for (Direction dir : Direction.values()) {
                for (BakedQuad quad : part.getQuads(dir)) {
                    extra.addUnculledFace(transformQuad(quad, height));
                }
            }
            for (BakedQuad quad : part.getQuads(null)) {
                extra.addUnculledFace(transformQuad(quad, height));
            }
        }

        return new SimpleModelWrapper(
                extra.build(),
                false,
                particle
        );
    }


    private BakedQuad transformQuad(BakedQuad quad, float yOffset) {
        int[] data = quad.vertices().clone();

        for (int v = 0; v < 4; v++) {
            int i = v * 8;
            float x = Float.intBitsToFloat(data[i]);
            float y = Float.intBitsToFloat(data[i + 1]);
            float z = Float.intBitsToFloat(data[i + 2]);
            Vector3f pos = new Vector3f(
                    x - 0.5f,
                    y - 0.5f,
                    z - 0.5f
            );
            pos.rotate(rotation);
            pos.add(0.5f, 0.5f + yOffset, 0.5f);
            data[i]     = Float.floatToRawIntBits(pos.x());
            data[i + 1] = Float.floatToRawIntBits(pos.y());
            data[i + 2] = Float.floatToRawIntBits(pos.z());
        }
        return new BakedQuad(
                data,
                quad.tintIndex(),
                quad.direction(),
                quad.sprite(),
                false,
                LightTexture.FULL_BRIGHT
        );
    }
    *///?} else {
    @Override
    public @NotNull List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            @NotNull RandomSource random
    ) {

        List<BakedQuad> baseQuads = base.getQuads(state, side, random);
        List<BakedQuad> result = new ArrayList<>(baseQuads);

        if(state != null) {
            Minecraft mc = Minecraft.getInstance();
            BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

            BakedModel original = dispatcher.getBlockModel(state);

            ArrayList<BakedQuad> fresh = new ArrayList<>(original.getQuads(state, null, random));
            for (Direction direction : Direction.values()) {
                fresh.addAll(original.getQuads(state, direction, random));
            }
            for (BakedQuad quad : fresh) {
                result.add(transformQuad(quad, height));
            }
        }

        return result;
    }
    private BakedQuad transformQuad(BakedQuad quad, float yOffset) {
        int[] data = quad.getVertices().clone();

        for (int v = 0; v < 4; v++) {
            int i = v * 8;
            float x = Float.intBitsToFloat(data[i]);
            float y = Float.intBitsToFloat(data[i + 1]);
            float z = Float.intBitsToFloat(data[i + 2]);
            Vector3f pos = new Vector3f(
                    x - 0.5f,
                    y - 0.5f,
                    z - 0.5f
            );
            pos.rotate(rotation);
            pos.add(0.5f, 0.5f + yOffset, 0.5f);
            data[i]     = Float.floatToRawIntBits(pos.x());
            data[i + 1] = Float.floatToRawIntBits(pos.y());
            data[i + 2] = Float.floatToRawIntBits(pos.z());
        }
        return new BakedQuad(
                data,
                quad.getTintIndex(),
                quad.getDirection(),
                quad.getSprite(),
                false
                //? if >=1.21.3 {
                , LightTexture.FULL_BRIGHT
                //?}
        );
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }


    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return base.getParticleIcon();
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return base.getTransforms();
    }

    //? if <=1.21.1 {
    /*@Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
    *///?}

    //? if >=1.21.4 {
    @Override
    public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
        VanillaModelEncoder.emitBlockQuads(emitter, (BakedModel)this, state, randomSupplier, cullTest);
    }
    //?} else {
    /*@Override
    public boolean isCustomRenderer() {
        return true;
    }
    *///?}
    //?}
}
