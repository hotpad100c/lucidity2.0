package ml.mypals.lucidity.mixin.features.visualizers.sculkSensorRange;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ml.mypals.lucidity.features.visualizers.sculk.ISculkSensorBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SculkSensorBlock.class)
public abstract class SculkSensorBlockMixin extends BaseEntityBlock implements SimpleWaterloggedBlock {


    protected SculkSensorBlockMixin(Properties properties) {
        super(properties);
    }

    @WrapMethod(method = "getTicker")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType, Operation<BlockEntityTicker<T>> original) {
        return !level.isClientSide()?original.call(level,blockState,blockEntityType):
            createTickerHelper(
                blockEntityType, //Custom ticker if its client-side
                BlockEntityType.SCULK_SENSOR,
                (levelx, blockPos, blockStatex, sculkSensorBlockEntity) ->
                {
                    ISculkSensorBlockEntity sculkSensorBlock = (ISculkSensorBlockEntity) sculkSensorBlockEntity;
                    sculkSensorBlock.lucidity_sculk_visualizer$custom_tick();
                }
            );
    }
}
