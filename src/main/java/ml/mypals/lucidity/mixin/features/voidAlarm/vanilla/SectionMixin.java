package ml.mypals.lucidity.mixin.features.voidAlarm.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;

import static ml.mypals.lucidity.config.FeatureToggle.VOID_ALARM;

@Mixin(LevelChunkSection.class)
public class SectionMixin {
    @WrapMethod(method = "hasOnlyAir")
    private boolean doesChunkExistAt(Operation<Boolean> original) {
        if(VOID_ALARM.getBooleanValue()){
           return false;
        }
        return original.call();
    }
}
