package ml.mypals.lucidity.mixin.features.visualizers.mobSpawn;

import com.llamalad7.mixinextras.sugar.Local;
import ml.mypals.lucidity.features.visualizers.mobSpawn.SpawnTrace;
import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.line.StripLineShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.FeatureToggle.MOB_SPAWN_VISUALIZE;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    @Unique
    private static boolean spawning = false;
    @Unique
    private static final ThreadLocal<SpawnTrace> CURRENT_TRACE = ThreadLocal.withInitial(SpawnTrace::new);


    @Inject(method =
            "spawnCategoryForChunk(Lnet/minecraft/world/entity/MobCategory;" +
                    "Lnet/minecraft/server/level/ServerLevel;" +
                    "Lnet/minecraft/world/level/chunk/LevelChunk;" +
                    "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;" +
                    "Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At("HEAD"))
    private static void spawnCategoryForChunk(MobCategory mobCategory, ServerLevel serverLevel, LevelChunk levelChunk, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback, CallbackInfo ci) {
        spawning = true;
    }
    @Inject(method =
            "spawnCategoryForChunk(Lnet/minecraft/world/entity/MobCategory;" +
                    "Lnet/minecraft/server/level/ServerLevel;" +
                    "Lnet/minecraft/world/level/chunk/LevelChunk;" +
                    "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;" +
                    "Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At("RETURN"))
    private static void spawnCategoryForChunkEnd(MobCategory mobCategory, ServerLevel serverLevel, LevelChunk levelChunk, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback, CallbackInfo ci) {
        spawning = false;
    }
    @Inject(method =
            "spawnCategoryForPosition(" +
            "Lnet/minecraft/world/entity/MobCategory;" +
            "Lnet/minecraft/server/level/ServerLevel;" +
            "Lnet/minecraft/world/level/chunk/ChunkAccess;" +
            "Lnet/minecraft/core/BlockPos;" +
            "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;" +
            "Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;" +
            ")V",
            at = @At("HEAD"))
    private static void onSpawnStart(
            MobCategory category, ServerLevel level, ChunkAccess chunk,
            BlockPos initialPos, NaturalSpawner.SpawnPredicate predicate,
            NaturalSpawner.AfterSpawnCallback callback, CallbackInfo ci) {

        if(!MOB_SPAWN_VISUALIZE.getBooleanValue()) return;
        SpawnTrace trace = new SpawnTrace(initialPos,new ArrayList<>(),null);
        CURRENT_TRACE.set(trace);
    }

    @Inject(method =
            "spawnCategoryForPosition(" +
                    "Lnet/minecraft/world/entity/MobCategory;" +
                    "Lnet/minecraft/server/level/ServerLevel;" +
                    "Lnet/minecraft/world/level/chunk/ChunkAccess;" +
                    "Lnet/minecraft/core/BlockPos;" +
                    "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;" +
                    "Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;" +
                    ")V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;set(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;")
    )
    private static void captureOffsetStep(
            MobCategory category, ServerLevel level, ChunkAccess chunk,
            BlockPos initialPos, NaturalSpawner.SpawnPredicate predicate,
            NaturalSpawner.AfterSpawnCallback callback, CallbackInfo ci,
            @Local(ordinal = 0) int y,
            @Local(ordinal = 2) int k,
            @Local(ordinal = 3) int l, @Local(ordinal = 4) int m,
            @Local(ordinal = 8) int q
    ) {
        if(!MOB_SPAWN_VISUALIZE.getBooleanValue()) return;
        SpawnTrace trace = CURRENT_TRACE.get();
        if (trace.initial != null) {
            int dx = l - initialPos.getX();
            int dz = m - initialPos.getZ();

            trace.steps.add(new SpawnTrace.OffsetStep(
                    k + 1,
                    q + 1,
                    dx, dz,
                    new BlockPos(l, y, m)
            ));

            trace.finalCandidate = new BlockPos(l, y, m);
        }
    }

    @Inject(method =
            "spawnCategoryForPosition(" +
            "Lnet/minecraft/world/entity/MobCategory;" +
            "Lnet/minecraft/server/level/ServerLevel;" +
            "Lnet/minecraft/world/level/chunk/ChunkAccess;" +
            "Lnet/minecraft/core/BlockPos;" +
            "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;" +
            "Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;" +
            ")V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private static void onMobSpawned(CallbackInfo ci, @Local Mob mob) {

        if(!MOB_SPAWN_VISUALIZE.getBooleanValue()) return;
        SpawnTrace trace = CURRENT_TRACE.get();
        if (trace.initial != null && !trace.steps.isEmpty()) {
            BlockPos spawnedAt = mob.blockPosition();


            List<Vec3> pathPoints = new ArrayList<>();
            List<Color> generated = new ArrayList<>();


            ChunkPos chunkPos = new ChunkPos(trace.initial);
            Level level = mob.level();

            //? if >=1.21.3 {
            BlockPos minPos = new BlockPos(chunkPos.getMinBlockX(),level.getMinY(),chunkPos.getMinBlockZ());
            int levelMinY = level.getMinY();
            //?} else {
            /*BlockPos minPos = new BlockPos(chunkPos.getMinBlockX(),level.getMaxBuildHeight(),chunkPos.getMinBlockZ());
            int levelMinY = level.getMinBuildHeight();
            *///?}
            int chunkMinZ = chunkPos.getMinBlockZ();

            BlockPos p2 = new BlockPos(trace.initial.getX(), levelMinY, chunkMinZ);
            BlockPos p3 = new BlockPos(trace.initial.getX(), levelMinY, trace.initial.getZ());
            BlockPos p4 = trace.initial;

            pathPoints.add(minPos.getCenter());
            pathPoints.add(p2.getCenter());
            pathPoints.add(p3.getCenter());
            pathPoints.add(p4.getCenter());

            generated.add(Color.GRAY);
            generated.add(Color.RED);
            generated.add(Color.BLUE);
            generated.add(Color.GREEN);

            pathPoints.add(trace.initial.getCenter());
            generated.add(Color.red);
            for (var step : trace.steps) {
                pathPoints.add(step.pos().getCenter());
                if(step.pos().equals(spawnedAt))generated.add(Color.WHITE);
                else generated.add(Color.GRAY);
            }
            pathPoints.add(spawnedAt.getBottomCenter());

            StripLineShape path =
                    ShapeGenerator.generateStripLine()
                    .vertexes(pathPoints)
                    .vertexColors(generated)
                    .lineWidth(10.0F)
                    .color(Color.DARK_GRAY)
                    .seeThrough(true)
                    .transform((t) -> {

                        Minecraft minecraft = Minecraft.getInstance();
                        Shape shape = t.getShape();
                        int life = shape.getCustomData("lifeTime",0);

                        if(!MOB_SPAWN_VISUALIZE.getBooleanValue() || minecraft.level == null || t.getWidth(false) <= 0){
                            t.getShape().discard();
                            return;
                        }
                        life++;
                        shape.putCustomData("lifeTime",life);
                        if(!minecraft.level.tickRateManager().isFrozen() && life % 2 == 0) {
                            t.setWidth(
                                    Math.max(0, t.getWidth(false)
                                            - 0.5f)
                            );
                        }
                    })
               .build(Shape.RenderingType.BATCH);

            ShapeManagers.addShape(
                    ResourceLocation.fromNamespaceAndPath(
                            MOD_ID,"mob_spawn_trace/" + trace.toString().toLowerCase().replace("@","_")
                    ),path
            );

        }
    }

    @Inject(method = "spawnCategoryForPosition(" +
            "Lnet/minecraft/world/entity/MobCategory;" +
            "Lnet/minecraft/server/level/ServerLevel;" +
            "Lnet/minecraft/world/level/chunk/ChunkAccess;" +
            "Lnet/minecraft/core/BlockPos;" +
            "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;" +
            "Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;" +
            ")V", at = @At("RETURN"))
    private static void onSpawnEnd(CallbackInfo ci) {
        CURRENT_TRACE.remove();
    }

}