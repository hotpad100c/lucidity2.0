package ml.mypals.lucidity.mixin.features.blockNoRandomOffset;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

import static ml.mypals.lucidity.config.FeatureToggle.BLOCK_NO_RANDOM_OFFSET;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateMixin {
    @WrapMethod(method = "getOffset")
    //? if >=1.21.3 {
    public Vec3 getOffset(BlockPos blockPos, Operation<Vec3> original) {
        return Minecraft.getInstance() != null && BLOCK_NO_RANDOM_OFFSET.getBooleanValue()?Vec3.ZERO:original.call(blockPos);
    }
    //?} else {
    /*public Vec3 getOffset(BlockGetter blockGetter, BlockPos blockPos, Operation<Vec3> original) {
        return BLOCK_NO_RANDOM_OFFSET.getBooleanValue() ?Vec3.ZERO:original.call(blockGetter,blockPos);
    }
    *///?}
}
