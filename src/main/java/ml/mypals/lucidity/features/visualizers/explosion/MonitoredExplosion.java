package ml.mypals.lucidity.features.visualizers.explosion;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static ml.mypals.lucidity.config.ExplosionVisualizerConfigs.*;

//? if >=1.21.3 {
/*public class MonitoredExplosion implements Explosion {
*///?} else {
public class MonitoredExplosion extends Explosion {
//?}
    public @Nullable BlockPos explosionSourceBlock;

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private final Explosion.BlockInteraction blockInteraction;
    private final ClientLevel level;
    private final Vec3 center;
    @Nullable
    private final Entity source;
    private final float radius;
    private final ExplosionDamageCalculator damageCalculator;
    //? if <=1.21.1 {
    public MonitoredExplosion(ClientLevel level, @Nullable Entity entity,@Nullable BlockPos blockPos, @Nullable ExplosionDamageCalculator explosionDamageCalculator, Vec3 vec3, float g, BlockInteraction blockInteraction, ParticleOptions particleOptions, ParticleOptions particleOptions2, Holder<SoundEvent> holder) {
        super(level, entity, vec3.x(), vec3.y(), vec3.z(), g, List.of(), blockInteraction, particleOptions, particleOptions2, holder);
        this.level = level;
        this.source = entity;
        this.radius = g;
        this.center = vec3;
        this.blockInteraction = blockInteraction;
        this.damageCalculator = explosionDamageCalculator == null ? this.makeDamageCalculator(entity) : explosionDamageCalculator;
        this.explosionSourceBlock = blockPos;
        if(entity == null && explosionSourceBlock == null){
            throw new IllegalStateException();
        }
    }
    //?} else {
    /*public MonitoredExplosion(ClientLevel clientLevel, @Nullable Entity entity, @Nullable BlockPos blockPos, @Nullable ExplosionDamageCalculator explosionDamageCalculator, Vec3 vec3, float f, Explosion.BlockInteraction blockInteraction) {
        this.level = clientLevel;
        this.source = entity;
        this.radius = f;
        this.center = vec3;
        this.blockInteraction = blockInteraction;
        this.damageCalculator = explosionDamageCalculator == null ? this.makeDamageCalculator(entity) : explosionDamageCalculator;
        this.explosionSourceBlock = blockPos;
        if(entity == null && explosionSourceBlock == null){
            throw new IllegalStateException();
        }
    }
    *///?}
    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity);
    }

    public static float getSeenPercent(Vec3 vec3, Entity entity, @Nullable BlockPos ignore, TriConsumer<Vec3, Vec3, Boolean> consumer) {
        AABB aABB = entity.getBoundingBox();
        double d = (double)1.0F / ((aABB.maxX - aABB.minX) * (double)2.0F + (double)1.0F);
        double e = (double)1.0F / ((aABB.maxY - aABB.minY) * (double)2.0F + (double)1.0F);
        double f = (double)1.0F / ((aABB.maxZ - aABB.minZ) * (double)2.0F + (double)1.0F);
        double g = ((double)1.0F - Math.floor((double)1.0F / d) * d) / (double)2.0F;
        double h = ((double)1.0F - Math.floor((double)1.0F / f) * f) / (double)2.0F;
        if (!(d < (double)0.0F) && !(e < (double)0.0F) && !(f < (double)0.0F)) {
            int i = 0;
            int j = 0;

            for(double k = 0.0F; k <= (double)1.0F; k += d) {
                for(double l = 0.0F; l <= (double)1.0F; l += e) {
                    for(double m = 0.0F; m <= (double)1.0F; m += f) {
                        double n = Mth.lerp(k, aABB.minX, aABB.maxX);
                        double o = Mth.lerp(l, aABB.minY, aABB.maxY);
                        double p = Mth.lerp(m, aABB.minZ, aABB.maxZ);
                        Vec3 vec32 = new Vec3(n + g, o, p + h);
                        Vec3 end = vec3;
                        boolean blocked = false;
                        BlockHitResult hitResult = entity.level().clip(new ClipContext(vec32, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
                        if (hitResult.getType() == HitResult.Type.MISS) {
                            ++i;
                        }else if (hitResult.getType() == HitResult.Type.BLOCK) {
                            BlockPos hitPos = hitResult.getBlockPos();
                            if(!hitPos.equals(ignore)){
                                blocked = true;
                                end = hitResult.getLocation();
                            }else{
                                ++i;
                            }
                        }
                        if(SAMPLE_POINT_RAY_CAST.getBooleanValue()){
                            consumer.accept(vec32.subtract(entity.position()),end,blocked);
                        }else{
                            consumer.accept(vec32.subtract(entity.position()),vec3,false);
                        }
                        ++j;
                    }
                }
            }

            return (float)i / (float)j;
        } else {
            return 0.0F;
        }
    }

    public float radius() {
        return this.radius;
    }

    public @NotNull Vec3 center() {
        return this.center;
    }

    @Override
    public boolean canTriggerBlocks() {
        return false;
    }
    //? if >1.21.1 {
    /*@Override
    public boolean shouldAffectBlocklikeEntities() {
        boolean bl2 = this.source == null || !this.source.isInWater();
        boolean bl3 = this.source == null || this.source.getType() != EntityType.BREEZE_WIND_CHARGE && this.source.getType() != EntityType.WIND_CHARGE;
        return this.blockInteraction.shouldAffectBlocklikeEntities() && bl2 && bl3;
    }
    *///?}

    private BlockDestructionResult calculateExplodedPositions() {
        HashMap<BlockPos,Float> affects = new HashMap<>();

        float hMin = this.radius * 0.7F;
        float hMax = this.radius * 1.3F;

        List<BlockSamplePointData> blockSamplePointDatas = new ArrayList<>();

        for (Vec3 dir : ExplosionRays.RAYS) {
            double dx = dir.x;
            double dy = dir.y;
            double dz = dir.z;

            double x = center.x;
            double y = center.y;
            double z = center.z;

            float h = hMax;

            while (h > 0.0F) {
                BlockPos pos = BlockPos.containing(x, y, z);
                if (!level.isInWorldBounds(pos)) break;

                BlockState state = isSourceBlock(pos)? Blocks.AIR.defaultBlockState():level.getBlockState(pos);
                FluidState fluid = isSourceBlock(pos)? Fluids.EMPTY.defaultFluidState():level.getFluidState(pos);

                Optional<Float> resistance =
                        damageCalculator.getBlockExplosionResistance(
                                this, level, pos, state, fluid
                        );

                if (resistance.isPresent()) {
                    h -= (resistance.get() + 0.3F) * 0.3F;
                }


                if (h <= 0.0F) break;
                if(BLOCK_RAY_CAST.getBooleanValue()){
                    blockSamplePointDatas.add(new BlockSamplePointData(center.subtract(new Vec3(x,y,z)),h));
                }
                if (BLOCK_DESTRUCTION.getBooleanValue() && !isSourceBlock(pos) && damageCalculator.shouldBlockExplode(this, level, pos, state, h)) {
                    float requiredH = hMax - h;
                    float prob;

                    if (requiredH <= hMin) {
                        prob = 1.0F;
                    } else if (requiredH >= hMax) {
                        prob = 0.0F;
                    } else {
                        prob = (hMax - requiredH) / (hMax - hMin);
                    }

                    affects.merge(pos, prob, Math::max);
                }

                x += dx * 0.3;
                y += dy * 0.3;
                z += dz * 0.3;

                h -= 0.22500001F;
            }
        }

        return new BlockDestructionResult(affects,blockSamplePointDatas);
    }

    private EntityDamageResult hurtEntities() {
        HashMap<EntityWithSamplePoint, Float> damagedMap = new HashMap<>();
        float f = this.radius * 2.0F;
        int i = Mth.floor(this.center.x - (double)f - (double)1.0F);
        int j = Mth.floor(this.center.x + (double)f + (double)1.0F);
        int k = Mth.floor(this.center.y - (double)f - (double)1.0F);
        int l = Mth.floor(this.center.y + (double)f + (double)1.0F);
        int m = Mth.floor(this.center.z - (double)f - (double)1.0F);
        int n = Mth.floor(this.center.z + (double)f + (double)1.0F);

        for(Entity entity : this.level.getEntities(this.source, new AABB(i, k, m, j, l, n))) {

            boolean ignoreExplosion = false;
            try {
                ignoreExplosion = entity.ignoreExplosion(this);
            } catch (Throwable ignored) {
            }

            if (!isSourceEntity(entity) && !ignoreExplosion) {
                double d = Math.sqrt(entity.distanceToSqr(this.center)) / (double)f;
                if (d <= (double)1.0F) {
                    double e = entity.getX() - this.center.x;
                    double g = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.center.y;
                    double h = entity.getZ() - this.center.z;
                    double o = Math.sqrt(e * e + g * g + h * h);
                    if (o != (double)0.0F) {
                        boolean bl = this.damageCalculator.shouldDamageEntity(this, entity);
                        float p = this.damageCalculator.getKnockbackMultiplier(entity);
                        List<SamplePointData> samplePoints = new ArrayList<>();
                        float q = !bl && p == 0.0F ? 0.0F : getSeenPercent(this.center, entity, explosionSourceBlock,(start, end, hit)->samplePoints.add(new SamplePointData(start,end,hit)));
                        if (bl) {
                            float damage = this.damageCalculator.getEntityDamageAmount(this, entity/*? if >1.21.1 {*//*, q*//*?}*/);
                            EntityWithSamplePoint ewsp = new EntityWithSamplePoint(entity,samplePoints);
                            damagedMap.put(ewsp, damage);
                        }
                    }
                }
            }
        }

        return new EntityDamageResult(damagedMap);
    }
    private boolean isSourceEntity(Entity entity){
        return this.source != null && source == entity;
    }
    private boolean isSourceBlock(BlockPos pos){
        return this.explosionSourceBlock != null && explosionSourceBlock.equals(pos);
    }
    public ExplosionResult simulateExplode() {


        BlockDestructionResult blockDestructionResult = BLOCK_DESTRUCTION.getBooleanValue() || BLOCK_RAY_CAST.getBooleanValue()?this.calculateExplodedPositions():new BlockDestructionResult(new HashMap<>(),new ArrayList<>());
        EntityDamageResult entityDamageResult = ENTITY_SAMPLE_POINTS.getBooleanValue()?this.hurtEntities():new EntityDamageResult(new HashMap<>());
        return new ExplosionResult(this.source,this.center,blockDestructionResult,entityDamageResult,this.radius * 1.3F);
    }

    public ClientLevel getLevel() {
        return this.level;
    }

    //? if >1.21.1 {
    /*@Override
    public @NotNull ServerLevel level() {
        throw new UnsupportedOperationException("Not supported in simulated explosion!");
    }
    *///?}

    @Override
    public @NotNull BlockInteraction getBlockInteraction() {
        return blockInteraction;
    }

    @Nullable
    public LivingEntity getIndirectSourceEntity() {
        return /*? if >1.21.1 {*//*null*//*?} else {*/super.getIndirectSourceEntity()/*?}*/;
    }

    @Nullable
    public Entity getDirectSourceEntity() {
        return this.source;
    }
    public static class EntityDamageResult {
        public HashMap<EntityWithSamplePoint,Float> damaged;
        EntityDamageResult(HashMap<EntityWithSamplePoint,Float> damaged){
            this.damaged = damaged;
        }
        public HashMap<EntityWithSamplePoint,Float> damagedEntities(){
            return damaged;
        }
    }
    public record ExplosionResult(@Nullable Entity source, Vec3 center, BlockDestructionResult blockDestructionResult,EntityDamageResult entityDamageResult,float maxPower){
        public Vec3 getCenter(float delta){
            return source == null?center:source.getPosition(delta);
        }
    }
    public record BlockSamplePointData(Vec3 offsetFromCenter, float strength){
    }
    public record EntityWithSamplePoint(Entity entity, List<SamplePointData> samplePointsOffsets) {
        public List<SamplePointData> getSamplePoints(float deltaTick){
            Vec3 entityPos = entity.getPosition(deltaTick);
            for (SamplePointData offset: samplePointsOffsets){
                offset.start = entityPos.add(offset.relativeStart);
            }
            return samplePointsOffsets;
        }
    }
    public static class SamplePointData{
        public Vec3 relativeStart, start, end;
        public boolean blocked;
        public SamplePointData(Vec3 relativeStart,Vec3 end,boolean blocked){
            this.relativeStart = relativeStart;
            this.start = Vec3.ZERO;
            this.end = end;
            this.blocked = blocked;
        }
    }
    public record BlockDestructionResult(HashMap<BlockPos,Float> affects,List<BlockSamplePointData> samples) {

    }
    public static final class ExplosionRays {

        public static final Vec3[] RAYS;

        static {
            List<Vec3> list = new ArrayList<>(1352);
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int l = 0; l < 16; ++l) {
                        if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                            double x = j / 15.0 * 2.0 - 1.0;
                            double y = k / 15.0 * 2.0 - 1.0;
                            double z = l / 15.0 * 2.0 - 1.0;
                            double len = Math.sqrt(x * x + y * y + z * z);
                            list.add(new Vec3(x / len, y / len, z / len));
                        }
                    }
                }
            }
            RAYS = list.toArray(Vec3[]::new);
        }
    }
}
