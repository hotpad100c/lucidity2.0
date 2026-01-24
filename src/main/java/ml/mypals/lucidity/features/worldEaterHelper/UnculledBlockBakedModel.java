package ml.mypals.lucidity.features.worldEaterHelper;

//? if >=1.21.5 {
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.impl.renderer.VanillaBlockModelPartEncoder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
//?} else {
/*import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
*///?}
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
//? if >=1.21.5 {

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
//?} else {
/*public class UnculledBlockBakedModel extends SimpleBakedModel {

    public final ArrayList<BakedQuad> allFaces = new ArrayList<>();
    public UnculledBlockBakedModel(BakedModel base, BlockState state, RandomSource random) {
        super(new ArrayList<>(),new HashMap<>(),false,base.isGui3d(), base.usesBlockLight(), base.getParticleIcon(),base.getTransforms()/^? if <=1.21.1 {^//^,base.getOverrides()^//^?}^/);
        allFaces.addAll(base.getQuads(state, null, random));
        for (Direction direction : Direction.values()) {
            allFaces.addAll(base.getQuads(state, direction, random));
        }
    }
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, @NotNull RandomSource randomSource) {
        return allFaces;
    }
}
*///?}