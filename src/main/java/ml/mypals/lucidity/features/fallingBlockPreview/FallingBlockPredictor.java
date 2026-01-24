package ml.mypals.lucidity.features.fallingBlockPreview;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class FallingBlockPredictor {

    private static final double GRAVITY = 0.04;
    private static final double DRAG = 0.98;
    private static final int MAX_TICKS = 600;

    @Nullable
    public static BlockPos predictLandingPos(Level level, double startX, double startY, double startZ, BlockState blockState) {
        double x = startX;
        double y = startY;
        double z = startZ;
        double vx = 0.0;
        double vy = 0.0;
        double vz = 0.0;

        boolean isConcretePowder = blockState.getBlock() instanceof ConcretePowderBlock;

        for (int tick = 0; tick < MAX_TICKS; tick++) {
            vy -= GRAVITY;

            x += vx;
            y += vy;
            z += vz;

            vx *= DRAG;
            vy *= DRAG;
            vz *= DRAG;

            BlockPos currentPos = BlockPos.containing(x, y, z);

            //? if >=1.21.3 {
            if (tick > 100 && (currentPos.getY() <= level.getMinY() || currentPos.getY() > level.getMaxY())) {
                return null;
            }
            //?} else {
            /*if (tick > 100 && (currentPos.getY() <= level.getMinBuildHeight() || currentPos.getY() > level.getMaxBuildHeight())) {
                return null;
            }
            *///?}

            BlockState stateAtPos = level.getBlockState(currentPos.below());
            boolean touchingWater = isConcretePowder && level.getFluidState(currentPos).is(FluidTags.WATER);

            if (!FallingBlock.isFree(stateAtPos) || touchingWater) {
                vx *= 0.7;
                vy *= -0.5;
                vz *= 0.7;

                if (!stateAtPos.is(Blocks.MOVING_PISTON)) {
                    boolean isFreeBelow = FallingBlock.isFree(level.getBlockState(currentPos.below()));
                    boolean canSurvive = blockState.canSurvive(level, currentPos) && !isFreeBelow;

                    if (canSurvive) {
                        return currentPos;
                    } else {
                        return null;
                    }
                }
            }

            if (Math.abs(vy) < 0.001 && !FallingBlock.isFree(level.getBlockState(currentPos.below()))) {
                BlockPos landPos = currentPos.above();
                BlockState landState = level.getBlockState(landPos);

                boolean canReplace = landState.canBeReplaced(
                        new DirectionalPlaceContext(level, landPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)
                );

                if (canReplace && blockState.canSurvive(level, landPos)) {
                    return landPos;
                }
                return null;
            }
        }

        return null;
    }
}