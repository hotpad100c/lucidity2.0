package ml.mypals.lucidity.features.visualizers.explosion;

import ml.mypals.ryansrenderingkit.builders.vertexBuilders.VertexBuilder;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.basics.core.LineLikeShape;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ml.mypals.lucidity.config.ExplosionVisualizerConfigs.*;
import static ml.mypals.lucidity.config.VisualizerColors.*;

public class ExplosionRayShape extends Shape implements LineLikeShape {

    public MonitoredExplosion.ExplosionResult explosionResult;
    private List<MonitoredExplosion.SamplePointData> explosionSamples = new ArrayList<>();
    protected ExplosionRayShape(RenderingType type) {
        super(type, (transform)->{}, Color.white, Vec3.ZERO, true);
    }


    public void generateRawGeometry(boolean lerp) {
        return;
    }

    public void setSampleData(List<MonitoredExplosion.SamplePointData> explosionSamples){
        this.explosionSamples = explosionSamples;
    }

    protected void drawInternal(VertexBuilder builder) {
        if(this.explosionSamples == null) return;

        float deltaTick = this.transformer.getTickDelta();
        if(ENTITY_SAMPLE_POINTS.getBooleanValue() && SAMPLE_POINT_RAY_CAST.getBooleanValue()) drawEntitySamples(builder,deltaTick);
    }

    private void drawEntitySamples(VertexBuilder builder,float deltaTick) {

        explosionSamples.clear();
        extractEntitySamplePoints(deltaTick);

        for (MonitoredExplosion.SamplePointData samplePointData : this.explosionSamples) {

            int startColor = samplePointData.blocked?SAMPLE_POINT_SAFE_COLOR.getColor().intValue:SAMPLE_POINT_EXPOSED_COLOR.getColor().intValue;
            builder.putColor(startColor);
            addLineSegment(builder, samplePointData.end, samplePointData.start,getLineWidth(true));
        }
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
    @Override
    public void setLineWidth(float v) {

    }

    @Override
    public float getLineWidth(boolean b) {
        return 0;
    }
}
