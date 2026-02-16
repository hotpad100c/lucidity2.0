package ml.mypals.lucidity.features.selectiveRendering;

//? if <1.21.9 {

public class SelectiveRenderingSubmitNodeStorage {

}
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;

public class SelectiveRenderingSubmitNodeStorage extends SubmitNodeStorage implements SubmitNodeCollector {

    private SubmitNodeCollector base;
    public SelectiveRenderingSubmitNodeStorage(SubmitNodeCollector submitNodeStorage){
        base = submitNodeStorage;
    }

    public <S> void submitModel(Model<? super S> model, S object, PoseStack poseStack, RenderType renderType, int i, int j, int k, @Nullable TextureAtlasSprite textureAtlasSprite, int l, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        base.order(0).submitModel(model, object, poseStack, new SelectiveRenderingRenderTypeWrapper(renderType), i, j, k, textureAtlasSprite, l, crumblingOverlay);
    }

    public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite, boolean bl, boolean bl2, int k, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int l) {
        base.order(0).submitModelPart(modelPart, poseStack, new SelectiveRenderingRenderTypeWrapper(renderType), i, j, textureAtlasSprite, bl, bl2, k, crumblingOverlay, l);
    }

    public void submitBlockModel(PoseStack poseStack, RenderType renderType, BlockStateModel blockStateModel, float f, float g, float h, int i, int j, int k) {
        base.order(0).submitBlockModel(poseStack, new SelectiveRenderingRenderTypeWrapper(renderType), blockStateModel, f, g, h, i, j, k);
    }

    public void submitItem(PoseStack poseStack, ItemDisplayContext itemDisplayContext, int i, int j, int k, int[] is, List<BakedQuad> list, RenderType renderType, ItemStackRenderState.FoilType foilType) {
        base.order(0).submitItem(poseStack, itemDisplayContext, i, j, k, is, list, new SelectiveRenderingRenderTypeWrapper(renderType), foilType);
    }

    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer) {
        base.order(0).submitCustomGeometry(poseStack, new SelectiveRenderingRenderTypeWrapper(renderType), customGeometryRenderer);
    }

    public void submitHitbox(PoseStack poseStack, EntityRenderState entityRenderState, HitboxesRenderState hitboxesRenderState) {
        base.order(0).submitHitbox(poseStack, entityRenderState, hitboxesRenderState);
    }

    public void submitShadow(PoseStack poseStack, float f, List<EntityRenderState.ShadowPiece> list) {
        base.order(0).submitShadow(poseStack, f, list);
    }

    public void submitNameTag(PoseStack poseStack, @Nullable Vec3 vec3, int i, Component component, boolean bl, int j, double d, CameraRenderState cameraRenderState) {
        base.order(0).submitNameTag(poseStack, vec3, i, component, bl, j, d, cameraRenderState);
    }

    public void submitText(PoseStack poseStack, float f, float g, FormattedCharSequence formattedCharSequence, boolean bl, Font.DisplayMode displayMode, int i, int j, int k, int l) {
        base.order(0).submitText(poseStack, f, g, formattedCharSequence, bl, displayMode, i, j, k, l);
    }

    public void submitFlame(PoseStack poseStack, EntityRenderState entityRenderState, Quaternionf quaternionf) {
        base.order(0).submitFlame(poseStack, entityRenderState, quaternionf);
    }

    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
        base.order(0).submitLeash(poseStack, leashState);
    }

    public void submitBlock(PoseStack poseStack, BlockState blockState, int i, int j, int k) {
        base.order(0).submitBlock(poseStack, blockState, i, j, k);
    }

    public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {
        base.order(0).submitMovingBlock(poseStack, movingBlockRenderState);
    }

    public void submitParticleGroup(SubmitNodeCollector.ParticleGroupRenderer particleGroupRenderer) {
        base.order(0).submitParticleGroup(particleGroupRenderer);
    }

    public void clear() {
        ((SubmitNodeStorage)base).clear();
    }

    public void endFrame() {
        ((SubmitNodeStorage)base).endFrame();
    }

    @Override
    public SubmitNodeCollection order(int i) {
        return ((SubmitNodeStorage)base).order(i);
    }
}
*///?}

