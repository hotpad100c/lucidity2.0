package ml.mypals.lucidity.features.worldEaterHelper;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.impl.renderer.VanillaBlockModelPartEncoder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

import java.util.function.Predicate;
public class UnculledBlockBakedModel implements BlockStateModel {
    private BlockStateModel blockStateModel;
    public UnculledBlockBakedModel(BlockStateModel base, BlockState state, RandomSource random) {
        this.blockStateModel = base;
    }

    @Override
    public void collectParts(@NotNull RandomSource randomSource, @NotNull List<BlockModelPart> list) {
        blockStateModel.collectParts(randomSource,list);
    }

    @Override
    public @NotNull TextureAtlasSprite particleIcon() {
        return blockStateModel.particleIcon();
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        blockStateModel.emitQuads(emitter, blockView, pos, state, random, direction -> false);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random) {
        return blockStateModel.createGeometryKey(blockView, pos, state, random);
    }

    @Override
    public TextureAtlasSprite particleSprite(BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
        return blockStateModel.particleSprite(blockView, pos, state);
    }
}
