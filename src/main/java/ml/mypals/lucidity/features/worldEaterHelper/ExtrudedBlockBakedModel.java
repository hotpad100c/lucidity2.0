package ml.mypals.lucidity.features.worldEaterHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.resources.model.QuadCollection;
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

public class ExtrudedBlockBakedModel implements BlockStateModel {

    private final BlockStateModel base;
    private final float height;
    private final Quaternionf rotation;

    public ExtrudedBlockBakedModel(BlockStateModel base, float height) {
        this.base = base;
        this.height = height;
        this.rotation = new Quaternionf()
                .rotateTo(
                        new Vector3f(1, 1, 1).normalize(),
                        new Vector3f(0, 1, 0)
                )
                .rotateY((float) Math.toRadians(45));
    }

    @Override
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
        // 1.21.11 起 BakedQuad 是 record，顶点位置直接以 Vector3fc 暴露，
        // 不用再手工拆那个打包过的 int[]。
        Vector3f[] positions = new Vector3f[4];
        for (int v = 0; v < 4; v++) {
            Vector3f pos = new Vector3f(quad.position(v));
            pos.sub(0.5f, 0.5f, 0.5f);
            pos.rotate(rotation);
            pos.add(0.5f, 0.5f + yOffset, 0.5f);
            positions[v] = pos;
        }
        return new BakedQuad(
                positions[0], positions[1], positions[2], positions[3],
                quad.packedUV(0), quad.packedUV(1), quad.packedUV(2), quad.packedUV(3),
                quad.tintIndex(),
                quad.direction(),
                quad.sprite(),
                false,
                LightTexture.FULL_BRIGHT
        );
    }
}
