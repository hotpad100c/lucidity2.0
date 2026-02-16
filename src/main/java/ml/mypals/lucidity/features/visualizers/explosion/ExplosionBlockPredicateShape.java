package ml.mypals.lucidity.features.visualizers.explosion;

import ml.mypals.lucidity.utils.LucidityColorHelper;
import ml.mypals.ryansrenderingkit.builders.vertexBuilders.VertexBuilder;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
//? if >=1.21.5 {
/*import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
*///?} else {
import net.minecraft.client.resources.model.BakedModel;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;

import static ml.mypals.lucidity.config.ExplosionVisualizerConfigs.*;
import static ml.mypals.lucidity.config.VisualizerColors.*;

public class ExplosionBlockPredicateShape extends Shape {

    private static final Logger log = LoggerFactory.getLogger(ExplosionBlockPredicateShape.class);
    private final ExplosionRayShape subRayShape = new ExplosionRayShape(RenderingType.BATCH);
    private final Map<List<Vec3>, Float> explosionBlockMesh = new HashMap<>();
    private final List<MonitoredExplosion.SamplePointData> explosionSamples = new ArrayList<>();

    public MonitoredExplosion.ExplosionResult explosionResult;
    protected ExplosionBlockPredicateShape(RenderingType type) {
        super(type, (transform)->{}, Color.white, Vec3.ZERO, true);
    }


    public void submitSubShape(){
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(this.id.getNamespace(), this.id.getPath() + "_rays"),subRayShape);
    }

    public void updateExplosionResult(MonitoredExplosion.ExplosionResult explosionResult){
        subRayShape.explosionResult = explosionResult;
        this.explosionResult = explosionResult;
    }

    public void generateRawGeometry(boolean lerp) {
        explosionBlockMesh.clear();
        if(this.explosionResult == null) return;
        generateBlockGeometry(lerp);
    }

    private void generateBlockGeometry(boolean lerp) {
        Level level = Minecraft.getInstance().level;
        if(!BLOCK_DESTRUCTION.getBooleanValue() || level == null) return;
        MonitoredExplosion.BlockDestructionResult blockResult = explosionResult.blockDestructionResult();
        if (blockResult == null || blockResult.affects() == null) {
            return;
        }

        HashMap<BlockPos, Float> affects = blockResult.affects();

        float maxAffect = 1.0f;
        float minAffect = 0.0f;

        for (Map.Entry<BlockPos, Float> entry : affects.entrySet()) {
            BlockPos pos = entry.getKey();
            float affectValue = entry.getValue();

            BlockState blockState = level.getBlockState(pos);
            if (blockState.isAir()) {
                continue;
            }

            List<Vec3> blockVertices = generateBlockVertices(pos, blockState,affects);

            float configAlpha = EXPLOSION_DESTRUCTION_COLOR.getColor().a;
            float normalizedAffect = (affectValue - minAffect) / (maxAffect - minAffect);
            float minAlpha = configAlpha * 0.05f;
            float alpha = minAlpha + normalizedAffect * (configAlpha - minAlpha);

            explosionBlockMesh.put(blockVertices, alpha);
        }
    }
    public static List<Vec3> parseQuadToTriangles(BakedQuad quad,BlockPos pos) {
        //? if >=1.21.5 {
        /*int[] vertices = quad.vertices();
        *///?} else {
        int[] vertices = quad.getVertices();
        //?}
        Vec3 v0 = extractVertexPosition(vertices, 0).add(pos.getX(),pos.getY(),pos.getZ());
        Vec3 v1 = extractVertexPosition(vertices, 1).add(pos.getX(),pos.getY(),pos.getZ());
        Vec3 v2 = extractVertexPosition(vertices, 2).add(pos.getX(),pos.getY(),pos.getZ());
        Vec3 v3 = extractVertexPosition(vertices, 3).add(pos.getX(),pos.getY(),pos.getZ());

        List<Vec3> triangle1 = new ArrayList<>();
        triangle1.add(v0);
        triangle1.add(v1);
        triangle1.add(v2);
        List<Vec3> triangles = new ArrayList<>(triangle1);

        List<Vec3> triangle2 = new ArrayList<>();
        triangle2.add(v0);
        triangle2.add(v2);
        triangle2.add(v3);
        triangles.addAll(triangle2);

        return triangles;
    }
    private static Vec3 extractVertexPosition(int[] vertices, int vertexIndex) {
        int startIndex = vertexIndex * 8;

        float x = Float.intBitsToFloat(vertices[startIndex]);
        float y = Float.intBitsToFloat(vertices[startIndex + 1]);
        float z = Float.intBitsToFloat(vertices[startIndex + 2]);

        return new Vec3(x, y, z);
    }
    private List<Vec3> generateBlockVertices(BlockPos pos, BlockState blockState,HashMap<BlockPos, Float> affects) {
        List<Vec3> vertices = new ArrayList<>();

        assert Minecraft.getInstance().level != null;
        Level level = Minecraft.getInstance().level;
        //? if >=1.21.5 {
        /*BlockStateModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
        for(BlockModelPart blockModelPart : blockModel.collectParts(level.getRandom()))
        {
            for (Direction direction:Direction.values()){
                BlockPos relativePos = pos.relative(direction);
                if(!affects.containsKey(relativePos) || Block.shouldRenderFace(blockState,level.getBlockState(relativePos),direction)){
                    for(BakedQuad quad : blockModelPart.getQuads(direction)){
                        vertices.addAll(parseQuadToTriangles(quad,pos));
                    }
                    for(BakedQuad quad : blockModelPart.getQuads(null)){
                        vertices.addAll(parseQuadToTriangles(quad,pos));
                    }
                }
            }
        }

        *///?} else {
        BakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
        for (Direction direction:Direction.values()){
            BlockPos relativePos = pos.relative(direction);
            //? if >=1.21.3 {
            /*if(!affects.containsKey(relativePos) || Block.shouldRenderFace(blockState,level.getBlockState(relativePos),direction)){
            *///?} else {
            if(!affects.containsKey(relativePos) || Block.shouldRenderFace(blockState,level,pos,direction,relativePos)){
            //?}
                for(BakedQuad quad : blockModel.getQuads(blockState,direction,level.getRandom())){
                    vertices.addAll(parseQuadToTriangles(quad,pos));
                }
            }
        }
        for(BakedQuad quad : blockModel.getQuads(blockState,null,level.getRandom())){
            vertices.addAll(parseQuadToTriangles(quad,pos));
        }
        //?}

        return vertices;
    }

    private void extractEntitySamplePoints(float deltaTick) {
        MonitoredExplosion.EntityDamageResult entityResult = explosionResult.entityDamageResult();
        if (entityResult == null || entityResult.damagedEntities() == null) {
            return;
        }
        HashMap<MonitoredExplosion.EntityWithSamplePoint, Float> damagedEntities = entityResult.damagedEntities();
        for (Map.Entry<MonitoredExplosion.EntityWithSamplePoint, Float> entry : damagedEntities.entrySet()) {
            MonitoredExplosion.EntityWithSamplePoint entityWithSample = entry.getKey();
            if(entityWithSample.entity() != Minecraft.getInstance().player || Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON)
                explosionSamples.addAll(entityWithSample.getSamplePoints(Minecraft.getInstance().level.tickRateManager().isFrozen() && entityWithSample.entity() != Minecraft.getInstance().player ?1.0f:deltaTick));
        }
    }

    protected void drawInternal(VertexBuilder builder) {
        if(this.explosionResult == null) return;
        float deltaTick = this.transformer.getTickDelta();
        if(BLOCK_DESTRUCTION.getBooleanValue()) drawBlockMesh(builder);
        if(ENTITY_SAMPLE_POINTS.getBooleanValue()) drawEntitySamples(builder,deltaTick);
        if(BLOCK_RAY_CAST.getBooleanValue()) drawBlockRays(builder,deltaTick);
        if(EXPLOSION_CENTER.getBooleanValue()) drawCenter(builder,deltaTick);
    }
    private void drawCenter(VertexBuilder builder,float deltaTick) {
        List<Vec3> facingTriangle = createPlayerFacingTriangle(explosionResult.getCenter(deltaTick),
                Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(),0.1f);
        builder.putColor(EXPLOSION_CENTER_COLOR.getColor().intValue);
        for (Vec3 vertex : facingTriangle) {
            builder.putVertex(vertex);
        }
    }
    private void drawBlockRays(VertexBuilder builder,float deltaTick) {

        Vec3 vec3 = explosionResult.getCenter(deltaTick);
        for(MonitoredExplosion.BlockSamplePointData data : explosionResult.blockDestructionResult().samples()){

            Vec3 pos = data.offsetFromCenter().reverse().add(vec3);
            if(vec3.distanceTo(pos)>0.1){
                List<Vec3> facingTriangle = createPlayerFacingTriangle(pos,
                        Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(),0.03f);

                float alpha = (data.strength() / explosionResult.maxPower())*255*2;

                Color color = LucidityColorHelper.invertColor(EXPLOSION_CENTER_COLOR.getColor().intValue);
                builder.putColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),(int)Math.min(255,alpha)));
                for (Vec3 vertex : facingTriangle) {
                    builder.putVertex(vertex);
                }
            }
        }
    }

    private void drawBlockMesh(VertexBuilder builder) {
        for (Map.Entry<List<Vec3>, Float> entry : this.explosionBlockMesh.entrySet()) {
            List<Vec3> vertices = entry.getKey();
            float alpha = entry.getValue();
            int color = getColorWithAlpha(alpha);
            builder.putColor(color);
            for (Vec3 vec3 : vertices) {
                builder.putVertex(vec3);
            }
        }
    }

    private void drawEntitySamples(VertexBuilder builder,float deltaTick) {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();


        explosionSamples.clear();
        extractEntitySamplePoints(deltaTick);

        for (MonitoredExplosion.SamplePointData center : this.explosionSamples) {
            List<Vec3> facingTriangle = createPlayerFacingTriangle(center.start,camPos,0.05f);
            int startColor = center.blocked?SAMPLE_POINT_SAFE_COLOR.getColor().intValue:SAMPLE_POINT_EXPOSED_COLOR.getColor().intValue;
            builder.putColor(startColor);
            for (Vec3 vertex : facingTriangle) {
                builder.putVertex(vertex);
            }
            if(center.blocked) {
                List<Vec3> returnTriangle = createPlayerFacingTriangle(center.end,camPos,0.03f);
                builder.putColor(LucidityColorHelper.invertColor(startColor));
                for (Vec3 vertex : returnTriangle) {
                    builder.putVertex(vertex);
                }
            }
        }
    }

    private List<Vec3> createPlayerFacingTriangle(Vec3 center,Vec3 camPos,float size ) {

        Vec3 toCamera = camPos.subtract(center).normalize();

        Vec3 up = new Vec3(0, 1, 0);

        if (Math.abs(toCamera.dot(up)) > 0.99) {
            up = new Vec3(1, 0, 0);
        }

        Vec3 right = toCamera.cross(up).normalize();
        Vec3 actualUp = right.cross(toCamera).normalize();

        float height = size * (float)Math.sqrt(3) / 2;

        List<Vec3> triangle = new ArrayList<>(3);

        triangle.add(center.add(actualUp.scale(height * 2.0 / 3.0)));

        triangle.add(center
                .add(actualUp.scale(-height / 3.0))
                .add(right.scale(-size / 2.0)));

        triangle.add(center
                .add(actualUp.scale(-height / 3.0))
                .add(right.scale(size / 2.0)));

        return triangle;
    }

    private int getColorWithAlpha(float alpha) {
        int a = (int)(alpha * 255);
        int r = (int) (EXPLOSION_DESTRUCTION_COLOR.getColor().r*255);
        int g = (int) (EXPLOSION_DESTRUCTION_COLOR.getColor().g*255);
        int b = (int) (EXPLOSION_DESTRUCTION_COLOR.getColor().b*255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    @Override
    public void discard(){
        subRayShape.discard();
        super.discard();
    }
}
