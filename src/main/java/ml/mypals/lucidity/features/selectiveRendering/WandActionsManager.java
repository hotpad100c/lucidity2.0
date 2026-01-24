package ml.mypals.lucidity.features.selectiveRendering;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.lucidity.Lucidity;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.box.BoxWireframeShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.SelectiveRenderingConfigs.*;
import static ml.mypals.lucidity.features.selectiveRendering.IntersectionResolver.cutBox;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.*;
import static ml.mypals.lucidity.hotkeys.HotkeyCallbacks.*;
import static ml.mypals.lucidity.utils.LucidityConfigHelper.addToConfigList;
import static ml.mypals.lucidity.utils.LucidityConfigHelper.removeFromConfigList;

public class WandActionsManager {

    public static BlockPos pos1;
    public static BlockPos pos2;
    public static BoxWireframeShape infoBox;
    public static int SELECT_COOLDOWN = 5;
    public static int selectCoolDown = SELECT_COOLDOWN;
    public static boolean deleteMode = false;
    public static BlockPos pointingPos;
    public enum WandApplyToMode implements IConfigOptionListEntry {
        APPLY_TO_BLOCKS("lucidity.info.wand.apply_to_blocks","BLOCKS","textures/gui/rendering_mode/wand_mode_blocks.png"),
        APPLY_TO_ENTITIES("lucidity.info.wand.apply_to_entities","ENTITIES","textures/gui/rendering_mode/wand_mode_entities.png"),
        APPLY_TO_PARTICLES("lucidity.info.wand.apply_to_particles","PARTICLES","textures/gui/rendering_mode/wand_mode_particles.png");
        private final String translationKey;
        private final String shortName;
        private final String icon;
        private static final WandApplyToMode[] VALUES = values();

        WandApplyToMode(String translationKey, String shortName, String icon) {
            this.translationKey = translationKey;
            this.shortName = shortName;
            this.icon = icon;
        }
        public String getTranslationKey() {
            return translationKey;
        }
        public String getIcon() {
            return icon;
        }

        @Override
        public String getStringValue() {
            return shortName;
        }

        @Override
        public String getDisplayName() {
            return Component.translatable(translationKey).getString();
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            int delta = forward ? 1 : -1;
            int next = (this.ordinal() + delta + VALUES.length) % VALUES.length;
            return VALUES[next];
        }

        @Override
        public IConfigOptionListEntry fromString(String s) {
            if (s == null) {
                return this;
            }
            for (WandApplyToMode mode : VALUES) {
                if (mode.name().equalsIgnoreCase(s)
                        || mode.shortName.equalsIgnoreCase(s)
                        || mode.translationKey.equalsIgnoreCase(s)) {
                    return mode;
                }
            }
            return this;
        }
    }
    public static void selectingAction(BlockPos pos, InteractionHand hand, Player player, Boolean isFirstPoint) {
        if (isFirstPoint) {
            pos1 = pos;
            player.swing(hand);
            player.playSound(SoundEvents.CHAIN_PLACE);
            if(pos1 != null){
                player.playSound(SoundEvents.CHAIN_PLACE);
            }
        } else{
            pos2 = pos;
            player.swing(hand);
            player.playSound(SoundEvents.CHAIN_PLACE);
            if(pos2 != null){
                player.playSound(SoundEvents.CHAIN_PLACE);
            }
        }
    }
    public static void addAreaAction(BlockPos pos, InteractionHand hand, Player player, Level world) {
        if (pos1 != null && pos2 !=null) {
            AreaBox addBox = new AreaBox(pos1,pos2, Color.WHITE,0f,false);
            addToConfigList(SELECTED_AREAS,addBox.asString());
            pos1 = null;
            pos2 = null;
            player.playSound(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN);
        }else{
            player.displayClientMessage(Component.literal(Component.translatable("lucidity.info.cant_add_area").getString()), true);
            player.playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value());
        }
    }
    public static void cutAreaAction(Player player) {
        if (pos1 != null && pos2 !=null) {
            AreaBox cutBox = new AreaBox(pos1,pos2, Color.RED,0.2f,true);
            ArrayList<AreaBox> remainingBoxes = new ArrayList<>();
            AtomicBoolean deletedSomething = new AtomicBoolean(false);

            selectedAreas.forEach(targetArea->{
                remainingBoxes.addAll(cutBox(targetArea, cutBox));
                try {
                    removeFromConfigList(SELECTED_AREAS, targetArea.asString());
                    targetArea.destroy();
                }catch (Exception e){
                    System.out.println("SelectedAreas in config file is not same with current selectedAreas in-game!");
                }
                deletedSomething.set(true);
            });
            if(deletedSomething.get()){
                remainingBoxes.forEach(box->{
                    addToConfigList(SELECTED_AREAS,box.asString());
                });
            }
            pos1 = null;
            pos2 = null;
            player.playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value());
        }else{
            player.displayClientMessage(Component.literal(Component.translatable("lucidity.info.cant_add_area").getString()), true);
            player.playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value());
        }
    }
    public static void selectBlockTypeAction(BlockPos pos, InteractionHand hand, Player player, Level world) {

        String id = BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()).toString();
        if(world.getBlockState(pos).getBlock() == Blocks.AIR ){return;}
        if (!SELECTED_BLOCKS.getStrings().contains(id)) {
            addToConfigList(SELECTED_BLOCKS,id);
            player.displayClientMessage(Component.literal(Component.translatable("lucidity.info.add_type").getString() + id), true);
            player.playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE);
        } else {
            removeFromConfigList(SELECTED_BLOCKS,id);
            player.displayClientMessage(Component.literal(Component.translatable("lucidity.info.removed_type").getString() + id), true);
            player.playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value());
        }
        player.swing(hand);
    }
    public static void selectEntityTypeAction(Entity target, InteractionHand hand, Player player, Level world) {
        if(target == null ){return;}
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
        if (!SELECTED_ENTITIES.getStrings().contains(id)) {
            addToConfigList(SELECTED_ENTITIES,id);
            player.displayClientMessage(Component.literal(Component.translatable("lucidity.info.add_type").getString() + id), true);
            player.playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE);
        } else {
            removeFromConfigList(SELECTED_ENTITIES,id);
            player.displayClientMessage(Component.literal(Component.translatable("lucidity.info.removed_type").getString() + id), true);
            player.playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value());
        }
        player.swing(hand);
    }
    public static void switchRenderMod(boolean increase){
        if(selectCoolDown > 0){return;}
        switch (APPLY_TARGET_MODE.getOptionListValue()){
            case WandApplyToMode.APPLY_TO_BLOCKS -> {
                BLOCK_RENDERING_MODE.setOptionListValue(BLOCK_RENDERING_MODE.getOptionListValue().cycle(increase));
                scheduleChunkRebuild();
            }
            case WandApplyToMode.APPLY_TO_ENTITIES -> {
                ENTITY_RENDERING_MODE.setOptionListValue(ENTITY_RENDERING_MODE.getOptionListValue().cycle(increase));
            }
            case WandApplyToMode.APPLY_TO_PARTICLES -> {
                PARTICLE_RENDERING_MODE.setOptionListValue(PARTICLE_RENDERING_MODE.getOptionListValue().cycle(increase));
            }
        }
        selectCoolDown = SELECT_COOLDOWN;
    }
    public static void switchWandMod(boolean increase){
        APPLY_TARGET_MODE.setOptionListValue(APPLY_TARGET_MODE.getOptionListValue().cycle(increase));
    }
    public static void clearArea(BlockPos pos, InteractionHand hand, Player player, Level world){
        if((pos1 != null || pos2 != null)) {
            pos1 = null;
            pos2 = null;
            player.displayClientMessage(Component.literal(Component.translatable("lucidity.info.clear").getString()), true);
            player.playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value());
        }
    }
    public static List<AreaBox> getAreasToDelete(BlockPos pos, boolean delete) {
        List<AreaBox> result = new ArrayList<>();
        boolean deletedSomething = false;

        Vec3 center = Vec3.atCenterOf(pos);

        for (AreaBox area : selectedAreas) {
            if (!isInsideArea(center, area, false)) {
                continue;
            }

            result.add(area);

            if (delete) {
                try {
                    area.destroy();
                    removeFromConfigList(SELECTED_AREAS, area.asString(),false);
                    deletedSomething = true;
                } catch (Exception e) {
                    Lucidity.LOGGER.warn(
                            "SelectedAreas in config file is not in sync with in-game selectedAreas",
                            e
                    );
                }
            }
        }

        if (deletedSomething) {
            SELECTED_AREAS.setModified();
        }

        return result;
    }

    public static void wandActions(Minecraft client){
        if(client.level == null || client.player == null){return;}
        pointingPos = getPlayerLookedBlock(client.player, client.level).getBlockPos();
        boolean shouldSelect = client.player.getMainHandItem().getItem() == wand;
        deleteMode = false;
        selectCoolDown = selectCoolDown <= 0? 0 : selectCoolDown-1;
        //getMouseScroll();
        if(selectCoolDown <= 0 && shouldSelect && client.player != null){
            InteractionHand mainHand = client.player.getUsedItemHand();
            if (deleteArea.isDown()) {
                deleteMode = true;

                if(client.options.keyUse.isDown()){
                    getAreasToDelete(pointingPos,true);
                    client.player.swing(mainHand);
                }
                else if (pos1 != null && pos2 != null && client.options.keyAttack.isDown()) {
                    cutAreaAction(client.player);
                    selectCoolDown = SELECT_COOLDOWN;
                    client.player.swing(mainHand);
                }
            } else if (client.options.keyAttack.isDown()) {
                if (client.options.keyShift.isDown()) {
                    clearArea(client.player.getOnPos(), mainHand, client.player, client.level);
                    selectCoolDown = SELECT_COOLDOWN;
                } else if (switchRenderMode.isDown()){
                    switchRenderMod(false);
                    selectCoolDown = SELECT_COOLDOWN;
                }else if (addArea.isDown()){
                    addAreaAction(client.player.getOnPos(), mainHand, client.player, client.level);
                    selectCoolDown = SELECT_COOLDOWN;
                }else {
                    BlockHitResult blockBreakingRayCast = getPlayerLookedBlock(client.player, client.level);
                    selectingAction(blockBreakingRayCast.getBlockPos(), mainHand, client.player, true);
                    selectCoolDown = SELECT_COOLDOWN;
                }
            }
            else if (client.options.keyPickItem.isDown()) {
                if (switchRenderMode.isDown()){
                    switchWandMod(true);
                    selectCoolDown = SELECT_COOLDOWN;
                }
            }
            else if (client.options.keyUse.isDown()) {
                if (switchRenderMode.isDown()){
                    switchRenderMod(true);
                    selectCoolDown = SELECT_COOLDOWN;
                }else if (addArea.isDown()){
                    if(APPLY_TARGET_MODE.getOptionListValue() == WandApplyToMode.APPLY_TO_BLOCKS){
                        BlockHitResult blockBreakingRayCast = getPlayerLookedBlock(client.player, client.level);
                        selectBlockTypeAction(blockBreakingRayCast.getBlockPos(), mainHand, client.player, client.level);
                    }
                    if(APPLY_TARGET_MODE.getOptionListValue() == WandApplyToMode.APPLY_TO_ENTITIES){
                        EntityHitResult entityHitResult = getPlayerLookedEntity(client.player, client.level);
                        selectEntityTypeAction(entityHitResult.getEntity(), mainHand, client.player, client.level);
                    }
                    selectCoolDown = SELECT_COOLDOWN;
                } else {
                    BlockHitResult blockBreakingRayCast = getPlayerLookedBlock(client.player, client.level);
                    selectingAction(blockBreakingRayCast.getBlockPos(), mainHand, client.player, false);
                    selectCoolDown = SELECT_COOLDOWN;
                }
            }
            tickInfoBox();
        }
    }
    public static void tickInfoBox(){
        if(infoBox != null) {
            if(Minecraft.getInstance().level == null){
                infoBox.discard();
                infoBox = null;
                return;
            }
            infoBox.enabled = Minecraft.getInstance().player.getMainHandItem().is(wand);
            selectedAreas.forEach(areaBox -> {
                if(areaBox.boxShape == null){return;}
                areaBox.boxShape.enabled = infoBox.enabled;
            });
        }else {
            infoBox = ShapeGenerator.generateBoxWireframe()
                    .color(Color.WHITE)
                    .edgeWidth(2f)
                    .seeThrough(true)
                    .transform(boxTransformer -> {
                        Minecraft mc = Minecraft.getInstance();

                        if (mc.level == null || mc.player == null) {
                            return;
                        }

                        BlockPos lookAt = getPlayerLookedBlock(mc.player, mc.level).getBlockPos();

                        BlockPos a;
                        BlockPos b;

                        if (pos1 != null && pos2 != null) {
                            a = pos1;
                            b = pos2;
                        } else if (pos1 != null) {
                            a = pos1;
                            b = lookAt;
                        } else if (pos2 != null) {
                            a = pos2;
                            b = lookAt;
                        } else {
                            a = lookAt;
                            b = lookAt;
                        }

                        BoxWireframeShape shape = (BoxWireframeShape) boxTransformer.getShape();
                        infoBox.enabled = Minecraft.getInstance().player.getMainHandItem().is(wand);

                        int minX = Math.min(a.getX(), b.getX());
                        int minY = Math.min(a.getY(), b.getY());
                        int minZ = Math.min(a.getZ(), b.getZ());

                        int maxX = Math.max(a.getX(), b.getX());
                        int maxY = Math.max(a.getY(), b.getY());
                        int maxZ = Math.max(a.getZ(), b.getZ());

                        if(pos1 == null && pos2 == null){
                            shape.forceSetDimensions(new Vec3(1,1,1));
                            shape.setWorldPosition(lookAt.getCenter());
                        }else {
                            shape.setMin(new Vec3(minX, minY, minZ));
                            shape.setMax(new Vec3(maxX + 1, maxY + 1, maxZ + 1));
                        }
                    })

                    .build(Shape.RenderingType.BATCH);
            ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"selective_rendering_info_box"),infoBox);
        }
    }

    public static BlockHitResult getPlayerLookedBlock(Player player, Level world) {
        Entity camera = Minecraft.getInstance().getCameraEntity();
        assert camera != null;
        Vec3 start = camera.getEyePosition(1.0F);
        Vec3 end = start.add(camera.getViewVector(1.0F).scale(player.isCreative()?5:4));
        ClipContext context = new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player
        );
        return world.clip(context);
    }
    public static EntityHitResult getPlayerLookedEntity(Player player, Level world) {
        Entity camera = Minecraft.getInstance().getCameraEntity();
        assert camera != null;
        Vec3 start = camera.getViewVector(1.0F);
        Vec3 end = start.add(camera.getViewVector(1.0F).scale(player.isCreative()?5:4));
        AABB box = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0);
        return getEntityCollision(
                world,
                player,
                start,
                end,
                box,
                entity -> entity != player,
                0.5F
        );
    }

    public static EntityHitResult getEntityCollision(
            Level world,
            Entity entity,
            Vec3 min,
            Vec3 max,
            AABB box,
            Predicate<Entity> predicate,
            float margin
    ) {
        double closestDistance = Double.MAX_VALUE;
        Entity closestEntity = null;
        for (Entity target : world.getEntities(entity, box, predicate)) {
            AABB expandedBox = target.getBoundingBox().inflate(margin);

            Optional<Vec3> hitPos = expandedBox.clip(min, max);
            if (hitPos.isPresent()) {
                double distance = min.distanceToSqr(hitPos.get());
                if (distance < closestDistance) {
                    closestEntity = target;
                    closestDistance = distance;
                }
            }
        }
        return closestEntity == null ? null : new EntityHitResult(closestEntity);
    }
}
