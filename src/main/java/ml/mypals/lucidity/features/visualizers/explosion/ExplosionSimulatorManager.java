package ml.mypals.lucidity.features.visualizers.explosion;

import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.ExplosionVisualizerConfigs.ENABLE_EXPLOSION_VISUALIZER;
import static net.minecraft.world.level.block.BedBlock.PART;
import static net.minecraft.world.level.block.RespawnAnchorBlock.CHARGE;

public class ExplosionSimulatorManager {
    public static ConcurrentHashMap<BlockKey,ExplosionBlockPredicateShape> blockExplosions = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Entity,ExplosionBlockPredicateShape> entityExplosions = new ConcurrentHashMap<>();
    public static void tick(){
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if(!ENABLE_EXPLOSION_VISUALIZER.getBooleanValue() || level == null || minecraft.player == null){
            destroy();
            return;
        }

        findExplosivesInRange(level,minecraft.player.getOnPos());

        blockExplosions.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey().pos();
            Block expectedBlock = entry.getKey().block();
            Block actualBlock = level.getBlockState(pos).getBlock();
            if (pos.distSqr(minecraft.player.getOnPos()) > 12*12 || actualBlock != expectedBlock) {
                entry.getValue().discard();
                return true;
            }

            MonitoredExplosion monitoredExplosion = new MonitoredExplosion(
                    Minecraft.getInstance().level,
                    null,
                    pos,
                    getExplosionDamageCalculator(null,pos),
                    getExplosionCenter(null,pos),
                    getExplosionRadius(null,pos),
                    Explosion.BlockInteraction.DESTROY
                    //? if <=1.21.1 {
                    ,null
                    ,null
                    ,null
                    //?}
            );

            entry.getValue().updateExplosionResult(monitoredExplosion.simulateExplode());
            entry.getValue().generateRawGeometry(false);
            return false;
        });
        entityExplosions.entrySet().removeIf(entry -> {
            Entity entity = entry.getKey();
            if (entity.position().distanceTo(minecraft.player.position()) > 12 || !entity.isAlive() || entity.isRemoved()) {
                entry.getValue().discard();
                return true;
            }
            if(entity instanceof WitherBoss witherBoss && witherBoss.getInvulnerableTicks()<=0){
                entry.getValue().discard();
                return true;
            }

            MonitoredExplosion monitoredExplosion = new MonitoredExplosion(
                    Minecraft.getInstance().level,
                    entity,
                    null,
                    getExplosionDamageCalculator(entity,null),
                    getExplosionCenter(entity,null),
                    getExplosionRadius(entity,null),
                    Explosion.BlockInteraction.DESTROY
                    //? if <=1.21.1 {
                    ,null
                    ,null
                    ,null
                    //?}
            );

            entry.getValue().updateExplosionResult(monitoredExplosion.simulateExplode());
            entry.getValue().generateRawGeometry(false);

            return false;
        });
    }
    public record BlockKey(BlockPos pos, Block block) {
        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof BlockKey(BlockPos pos1, Block block1))) return false;
            return pos1.equals(pos) && block1 == block;
        }
    }

    public static void findExplosivesInRange(ClientLevel level, BlockPos playerPos) {
        int range = 12;
        AABB searchBox = AABB.encapsulatingFullBlocks(
                playerPos.offset(-range, -range, -range),
                playerPos.offset(range, range, range)
        );

        for (Entity entity : level.getEntities(null, searchBox)) {
            Vec3 center = getExplosionCenter(entity, null);
            Float radius = getExplosionRadius(entity, null);

            if (center != null && radius != 0) {
                entityExplosions.computeIfAbsent(entity,
                e -> {
                    ExplosionBlockPredicateShape explosionBlockPredicateShape= new ExplosionBlockPredicateShape(Shape.RenderingType.BATCH);
                    ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"entity_explosion_markers"+entity.getStringUUID().toLowerCase()),explosionBlockPredicateShape);
                    explosionBlockPredicateShape.submitSubShape();
                    return explosionBlockPredicateShape;
                });
            }
        }

        boolean respawnAnchorExplodes = !level.dimensionType().respawnAnchorWorks();
        boolean bedExplodes = !level.dimensionType().bedWorks();

        for (BlockPos pos : BlockPos.betweenClosed(
                /*? if >1.21.1 {*/
                /*searchBox
                *//*?} else {*/
                BlockPos.containing(searchBox.getMinPosition()),
                BlockPos.containing(searchBox.getMaxPosition())
                /*?}*/
        )){
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            boolean shouldAddExplosion = false;

            if (respawnAnchorExplodes && block == Blocks.RESPAWN_ANCHOR) {
                float radius = getExplosionRadius(null, pos);
                shouldAddExplosion = (radius != 0);
            }
            else if (bedExplodes && block instanceof BedBlock) {
                float radius = getExplosionRadius(null, pos);
                shouldAddExplosion = (radius != 0);
            }

            if (shouldAddExplosion) {
                BlockKey key = new BlockKey(pos.immutable(), block);
                blockExplosions.computeIfAbsent(key,
                    k -> {
                    ExplosionBlockPredicateShape explosionBlockPredicateShape= new ExplosionBlockPredicateShape(Shape.RenderingType.BATCH);
                        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"block_explosion_markers"+pos.hashCode()),explosionBlockPredicateShape);
                        explosionBlockPredicateShape.submitSubShape();
                        return explosionBlockPredicateShape;
                });
            }
        }
    }

    private static final ExplosionDamageCalculator TNT_MINECART_CACULATOR = new ExplosionDamageCalculator(){
        @Override
        public @NotNull Optional<Float> getBlockExplosionResistance(@NotNull Explosion explosion, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull FluidState fluidState) {
            return !blockState.is(BlockTags.RAILS) && !blockGetter.getBlockState(blockPos.above()).is(BlockTags.RAILS) ? super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState) : Optional.of(0.0F);
        }

        @Override
        public boolean shouldBlockExplode(@NotNull Explosion explosion, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull BlockState blockState, float f) {
            return !blockState.is(BlockTags.RAILS) && !blockGetter.getBlockState(blockPos.above()).is(BlockTags.RAILS) && super.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, f);
        }
    };
    private static final ExplosionDamageCalculator WIND_CHARGE_CACULATOR = new ExplosionDamageCalculator(){
        @Override
        public @NotNull Optional<Float> getBlockExplosionResistance(@NotNull Explosion explosion, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull FluidState fluidState) {
            return blockState.is(BuiltInRegistries.BLOCK.get(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS/*? if <=1.21.1 {*/.location()/*?}*/)/*? if >1.21.1 {*//*.get()*//*?}*/) ? Optional.of(3600000.0F) : Optional.empty();
        }

        @Override
        public boolean shouldBlockExplode(@NotNull Explosion explosion, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull BlockState blockState, float f) {
            return canTriggerBlocks(blockPos, blockGetter) && super.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, f);
        }
    };
    private static ExplosionDamageCalculator getExplosionDamageCalculator(@Nullable Entity entity,@Nullable BlockPos blockPos){
        ExplosionDamageCalculator entityCaculator = switch (entity){
            case AbstractWindCharge windCharge -> WIND_CHARGE_CACULATOR;
            case MinecartTNT minecartTNT -> TNT_MINECART_CACULATOR;
            case null, default -> null;
        };
        if(blockPos == null) return entityCaculator;
        Level level = Minecraft.getInstance().level;
        assert level != null;
        BlockState state = level.getBlockState(blockPos);
        return switch (state.getBlock()){
            case RespawnAnchorBlock respawnAnchorBlock ->{
                    Stream<Direction> var10000 = Direction.Plane.HORIZONTAL.stream();
                    boolean bl = var10000.map(blockPos::relative).anyMatch((blockPosx) ->
                        RespawnAnchorBlock.isWaterThatWouldFlow(blockPosx, level));
                    final boolean bl2 = bl || level.getFluidState(blockPos.above()).is(FluidTags.WATER);

                    yield new ExplosionDamageCalculator() {
                        public @NotNull Optional<Float> getBlockExplosionResistance(@NotNull Explosion explosion, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPosx, @NotNull BlockState blockState, @NotNull FluidState fluidState) {
                        return blockPos.equals(blockPosx) && bl2 ? Optional.of(Blocks.WATER.getExplosionResistance()) : super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState);
                    }
                };
            }
            default -> null;
        };

    }
    private static Vec3 getExplosionCenter(@Nullable Entity entity,@Nullable BlockPos blockPos) {
        Vec3 entityCenter = switch (entity) {
            case EndCrystal endCrystal -> entity.position();

            case PrimedTnt tnt -> {
                Vec3 tntPos = tnt.position();
                double tntPosY = tntPos.y + (double)tnt.getBbHeight() * 0.0625;
                yield new Vec3(tntPos.x, tntPosY, tntPos.z);
            }

            case WitherSkull witherSkull -> entity.position();

            case LargeFireball fireball -> entity.position();

            case WitherBoss wither -> {
                Vec3 pos = wither.position();
                if (wither.getInvulnerableTicks() > 0) {
                    yield new Vec3(pos.x, wither.getEyeY(), pos.z);
                }
                yield null;
            }

            case MinecartTNT tntMinecart -> tntMinecart.position();

            case Creeper creeper -> creeper.position();

            case BreezeWindCharge breezeWindCharge -> entity.position();

            case WindCharge windCharge -> entity.position();

            case null, default -> null;
        };
        return blockPos == null?entityCenter:blockPos.getCenter();
    }

    private static Float getExplosionRadius(@Nullable Entity entity,@Nullable BlockPos pos) {
        float entityRad = switch (entity) {
            case EndCrystal endCrystal -> 6.0f;

            case PrimedTnt tnt ->/*? if >1.21.1 {*//*tnt.explosionPower*//*?} else {*/4.0f/*?}*/;

            case WitherSkull witherSkull -> 1.0f;

            case LargeFireball fireball -> (float)fireball.explosionPower;

            case WitherBoss wither -> wither.getInvulnerableTicks() > 0 ? 7.0f : 0.0f;

            case MinecartTNT tntMinecart -> {
                double d = Math.sqrt(tntMinecart.getDeltaMovement().horizontalDistanceSqr());
                if (d > 5.0) {
                    d = 5.0;
                }
                yield (float)(4.0 + ThreadLocalRandom.current().nextDouble() * 1.5 * d);
            }

            case Creeper creeper -> {
                int explosionRadius = creeper.explosionRadius;
                float f = creeper.isPowered() ? 2.0F : 1.0F;
                yield explosionRadius * f;
            }

            case BreezeWindCharge breezeWindCharge -> 3.0f;

            case WindCharge windCharge -> 1.2f;

            case null, default -> 0f;
        };
        if(pos == null){
            return entityRad;
        }
        Level level = Minecraft.getInstance().level;
        assert level != null;
        BlockState blockState = level.getBlockState(pos);

        return switch (blockState.getBlock()) {
            case RespawnAnchorBlock respawnAnchorBlock -> blockState.getValue(CHARGE) != 0 ? 5.0f:0.0f;

            case BedBlock bedBlock -> blockState.getValue(PART) != BedPart.HEAD?0.0f:5.0f;

            default -> 0f;
        };
    }

    public static void destroy(){
        for(Map.Entry<BlockKey,ExplosionBlockPredicateShape> blockPredicateShapeEntry : blockExplosions.entrySet()){
            blockPredicateShapeEntry.getValue().discard();
        }
        blockExplosions.clear();
        for(Map.Entry<Entity,ExplosionBlockPredicateShape> entityPredicateShapeEntry : entityExplosions.entrySet()){
            entityPredicateShapeEntry.getValue().discard();
        }
        entityExplosions.clear();
    }
    public static boolean canTriggerBlocks(BlockPos pos, BlockGetter world) {
        Block block = world.getBlockState(pos).getBlock();
        return
                block instanceof CandleBlock
                || block instanceof DoorBlock
                || block instanceof ButtonBlock
                || block instanceof FenceGateBlock
                || block instanceof LeverBlock
                || block instanceof TrapDoorBlock
                || block instanceof BellBlock;
    }
}
