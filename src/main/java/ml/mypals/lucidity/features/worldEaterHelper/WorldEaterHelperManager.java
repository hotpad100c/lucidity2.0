package ml.mypals.lucidity.features.worldEaterHelper;

import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import ml.mypals.lucidity.utils.BlockMatchRule;
import net.minecraft.client.Minecraft;
//? if >=1.21.5 {
/*import net.minecraft.client.renderer.block.model.BlockStateModel;
*///?} else {
import net.minecraft.client.resources.model.BakedModel;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static ml.mypals.lucidity.config.LucidityConfigs.Generic.WORLD_EATER_MINE_HELPER_HEIGHT;
import static ml.mypals.lucidity.config.LucidityConfigs.Generic.WORLD_EATER_MINE_HELPER_TARGETS;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.scheduleChunkRebuild;
import static ml.mypals.lucidity.utils.BlockMatchRule.parseRule;

public class WorldEaterHelperManager {


    private static final CopyOnWriteArrayList<BlockMatchRule> RULES = new CopyOnWriteArrayList<>();

//? if >=1.21.5 {
    /*private static final Map<BlockStateModel, ExtrudedBlockBakedModel> CACHE = new IdentityHashMap<>();
    public static BlockStateModel getExtruded(BlockStateModel base) {

*///?} else {
    private static final Map<BakedModel, ExtrudedBlockBakedModel> CACHE = new IdentityHashMap<>();
    public static BakedModel getExtruded(BakedModel base) {
//?}
        return CACHE.computeIfAbsent(
                base,
                b -> new ExtrudedBlockBakedModel(b, WORLD_EATER_MINE_HELPER_HEIGHT.getFloatValue())
        );
    }

    public static void invalidateCaches() {
        CACHE.clear();
    }

    public static void refresh() {
        invalidateCaches();
        resolveTargetBlocksFromString(WORLD_EATER_MINE_HELPER_TARGETS.getStrings());
        Minecraft.getInstance().levelRenderer.allChanged();
    }

    public static void resolveTargetBlocksFromString(List<String> blockStrings) {
        RULES.clear();

        for (String raw : blockStrings) {
            String input = raw.replace(" ", "");
            try {
                RULES.add(parseRule(input));
            } catch (Exception e) {
                System.err.println("[Lucidity] Failed to parse rule: " + raw);
            }
        }

        scheduleChunkRebuild();
    }



    public static boolean shouldRender(BlockState state, BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        if (level == null || !isSelectedBlockType(state)) return false;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        int yMax = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        if (y < yMax) {
            int checked = 0;
            BlockPos.MutableBlockPos test = new BlockPos.MutableBlockPos();
            test.set(pos);
            for (Direction direction:Direction.values()){
                test.set(pos.relative(direction));
                BlockState relative = level.getBlockState(test);
                if (!SelectiveRenderingManager.shouldRenderBlock(relative, test)
                        || !relative.isSolidRender(/*? if <=1.21.1 {*/level,test/*?}*/)) {
                    return true;
                }
            }
            for (int i = y + 1; i <= yMax && checked < 20; ++i) {
                test.set(x, i, z);
                BlockState above = level.getBlockState(test);
                if (SelectiveRenderingManager.shouldRenderBlock(above, test) && above.isSolidRender(/*? if <=1.21.1 {*/level,test/*?}*/)) {
                    return false;
                }
                checked++;
            }
        }
        return true;
    }

    public static boolean isSelectedBlockType(BlockState state) {
        for (BlockMatchRule rule : RULES) {
            if (rule.matches(state)) return true;
        }
        return false;
    }
}
