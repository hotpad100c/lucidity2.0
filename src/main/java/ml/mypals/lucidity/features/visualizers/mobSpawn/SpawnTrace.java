package ml.mypals.lucidity.features.visualizers.mobSpawn;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnTrace {
    public BlockPos initial;
    public List<OffsetStep> steps;
    @Nullable
    public BlockPos finalCandidate;
    public SpawnTrace() { this(null, new ArrayList<>(), null); }

    public SpawnTrace(BlockPos initial, ArrayList<OffsetStep> objects, BlockPos finalCandidate) {
        this.initial = initial;
        this.steps = new ArrayList<>();
        this.finalCandidate = null;

    }

    public record OffsetStep(int attempt, int groupAttempt, int xOffset, int zOffset, BlockPos pos) {}
}