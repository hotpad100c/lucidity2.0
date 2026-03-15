package ml.mypals.lucidity.features.selectiveRendering;

import com.mojang.blaze3d.vertex.VertexFormat;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import ml.mypals.lucidity.Lucidity;
import ml.mypals.lucidity.utils.BlockMatchRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static ml.mypals.lucidity.config.SelectiveRenderingConfigs.*;
import static ml.mypals.lucidity.utils.BlockMatchRule.parseRule;

public class SelectiveRenderingManager {
    public static List<BlockMatchRule> selectedBlockTypes = new ArrayList<>();
    public static List<Integer> selectedEntityTypes = new CopyOnWriteArrayList<>();
    public static List<Integer> selectedParticleTypes = new CopyOnWriteArrayList<>();
    public static List<AreaBox> selectedAreas = new CopyOnWriteArrayList<>();

    private static Thread lightUpdateTask;

    public static Item wand;
    public enum SelectiveRenderingMode implements IConfigOptionListEntry {
        OFF(
                "config.lucidity.render_mode.off",
                "X",
                "textures/gui/rendering_mode/off.png"
        ),

        INSIDE_SPECIFIC(
                "config.lucidity.render_mode.inside_specific",
                "IS",
                "textures/gui/rendering_mode/inside_specific.png"
        ),
        INSIDE_NON_SPECIFIC(
                "config.lucidity.render_mode.inside_non_specific",
                "IN",
                "textures/gui/rendering_mode/inside_non_specific.png"
        ),
        INSIDE_ALL(
                "config.lucidity.render_mode.inside_all",
                "IA",
                "textures/gui/rendering_mode/inside_all.png"
        ),

        OUTSIDE_SPECIFIC(
                "config.lucidity.render_mode.outside_specific",
                "OS",
                "textures/gui/rendering_mode/outside_specific.png"
        ),
        OUTSIDE_NON_SPECIFIC(
                "config.lucidity.render_mode.outside_non_specific",
                "ON",
                "textures/gui/rendering_mode/outside_non_specific.png"
        ),
        OUTSIDE_ALL(
                "config.lucidity.render_mode.outside_all",
                "OA",
                "textures/gui/rendering_mode/outside_all.png"
        ),

        ANY_SPECIFIC(
                "config.lucidity.render_mode.any_specific",
                "AS",
                "textures/gui/rendering_mode/any_specific.png"
        ),
        ANY_NON_SPECIFIC(
                "config.lucidity.render_mode.any_non_specific",
                "AN",
                "textures/gui/rendering_mode/any_non_specific.png"
        );
        private final String translationKey;
        private final String shortName;
        private final String icon;
        private static final SelectiveRenderingMode[] VALUES = values();


        SelectiveRenderingMode(String translationKey, String shortName, String icon) {
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
            for (SelectiveRenderingMode mode : VALUES) {
                if (mode.name().equalsIgnoreCase(s)
                        || mode.shortName.equalsIgnoreCase(s)
                        || mode.translationKey.equalsIgnoreCase(s)) {
                    return mode;
                }
            }
            return this;
        }
    }

    public static void resolveSelectedBlockStatesFromString(List<String> blockStrings) {
        selectedBlockTypes.clear();
        for (String raw : blockStrings) {
            String input = raw.replace(" ", "").toLowerCase();
            try {
                selectedBlockTypes.add(parseRule(input));
            } catch (Exception e) {
                System.err.println("[Lucidity] Failed to parse rule: " + raw);
            }
        }
        scheduleChunkRebuild();
    }

    public static void resolveSelectedEntityTypesFromString(List<String> entityStrings){
        selectedEntityTypes.clear();

        entityStrings.forEach(entityString -> {
            try {
                if (!entityString.contains(":")) {
                    entityString = "minecraft:" + entityString;
                }
                ResourceLocation entityId = ResourceLocation.tryParse(entityString);
                //? if >=1.21.3 {
                EntityType<?> targetEntity = BuiltInRegistries.ENTITY_TYPE.getValue(entityId);
                //?} else {
                /*EntityType<?> targetEntity = BuiltInRegistries.ENTITY_TYPE.get(entityId);
                *///?}
                selectedEntityTypes.add(BuiltInRegistries.ENTITY_TYPE.getId(targetEntity));
            }catch (Exception e) {
                System.err.println("Failed to parse entity type: " + entityString);
            }
        });
    }
    public static void resolveSelectedParticleTypesFromString(List<String> particleStrings){
        selectedParticleTypes.clear();

        particleStrings.forEach(particleString -> {
            try {
                if (!particleString.contains(":")) {
                    particleString = "minecraft:" + particleString;
                }
                ResourceLocation particleId = ResourceLocation.tryParse(particleString);
                //? if >=1.21.3 {
                ParticleType<?> targetParticle = BuiltInRegistries.PARTICLE_TYPE.getValue(particleId);
                 //?} else {
                /*ParticleType<?> targetParticle = BuiltInRegistries.PARTICLE_TYPE.get(particleId);
                *///?}
                selectedParticleTypes.add(BuiltInRegistries.PARTICLE_TYPE.getId(targetParticle));
            }catch (Exception e) {
                System.err.println("Failed to parse particle type: " + particleString);
            }
        });
    }
    public static void resolveSelectedWandFromString(String name){
        Item last_wind = wand;
        try {
            if (!name.contains(":")) {
                name = "minecraft:" + name;
            }
            ResourceLocation id = ResourceLocation.tryParse(name);
            //? if >=1.21.3 {
            wand = BuiltInRegistries.ITEM.getValue(id);
             //?} else {
            /*wand = BuiltInRegistries.ITEM.get(id);
            *///?}
        }catch (Exception e) {
            name = "minecraft:breeze_rod";
            System.err.println("Failed to parse wand item: " + name);
            wand = last_wind;
        }
    }
    public static void resolveSelectedAreasFromString(List<String> areaStrings){List<AreaBox> newAreas = new ArrayList<>();

        for (String areaString : areaStrings) {
            try {
                newAreas.add(parseAABB(areaString));
            } catch (IllegalArgumentException e) {
                Lucidity.LOGGER.warn("Failed to parse area '{}': {}", areaString, e.getMessage());
            }
        }

        selectedAreas.forEach(AreaBox::destroy);
        selectedAreas.clear();

        newAreas.forEach(area -> {
            selectedAreas.add(area);
            area.submit();
        });
        scheduleChunkRebuild();
    }

    private static AreaBox parseAABB(String areaString) throws IllegalArgumentException {
        String[] parts = areaString.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid format. Expected x1,y1,z1:x2,y2,z2");
        }

        String[] startCoords = parts[0].split(",");
        String[] endCoords = parts[1].split(",");

        if (startCoords.length != 3 || endCoords.length != 3) {
            throw new IllegalArgumentException("Invalid coordinates. Expected x1,y1,z1:x2,y2,z2");
        }

        try {
            int x1 = Integer.parseInt(startCoords[0].trim());
            int y1 = Integer.parseInt(startCoords[1].trim());
            int z1 = Integer.parseInt(startCoords[2].trim());

            int x2 = Integer.parseInt(endCoords[0].trim());
            int y2 = Integer.parseInt(endCoords[1].trim());
            int z2 = Integer.parseInt(endCoords[2].trim());

            int hash = new BlockPos(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2)).hashCode();
            float hue = (hash % 360) / 360.0f;
            float saturation = 1f;
            float brightness = 1f;
            Color color = Color.getHSBColor(hue, saturation, brightness);

            return new AreaBox(
                    new BlockPos(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2)),
                    new BlockPos(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2))
                    ,color,0.2f,false
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in input: " + areaString, e);
        }
    }

    public static boolean shouldRenderBlock(BlockPos pos) {
        return Minecraft.getInstance().level != null && shouldRenderBlock(Minecraft.getInstance().level.getBlockState(pos), pos);
    }
    public static boolean shouldRenderBlock(BlockState block, BlockPos pos) {

        boolean render = true;
        if(block.getBlock() instanceof MovingPistonBlock && Minecraft.getInstance().level != null){
            BlockEntity entity = Minecraft.getInstance().level.getBlockEntity(pos);
            if(entity instanceof PistonMovingBlockEntity piston){
                BlockState content = piston.getMovedState();

                BlockPos dist = entity.getBlockPos().relative(piston.getMovementDirection().getOpposite()).immutable();
                render = shouldRender(
                        BLOCK_RENDERING_MODE.getOptionListValue(),
                        content,
                        new Vec3(dist.getX(), dist.getY(), dist.getZ()),
                        blockType -> BuiltInRegistries.BLOCK.getId(content.getBlock()),
                        null,
                        selectedBlockTypes
                );

            }
        }
        render = render && shouldRender(
                BLOCK_RENDERING_MODE.getOptionListValue(),
                block,
                new Vec3(pos.getX(), pos.getY(), pos.getZ()),
                blockType -> BuiltInRegistries.BLOCK.getId(block.getBlock()),
                null,
                selectedBlockTypes
        );
        return render;
    }

    public static boolean shouldRenderEntity(EntityType<?> entity, Vec3 pos) {

        return shouldRender(
                ENTITY_RENDERING_MODE.getOptionListValue(),
                entity,
                pos,
                entityType -> BuiltInRegistries.ENTITY_TYPE.getId(entity),
                selectedEntityTypes,
                null
        );
    }

    public static boolean shouldRenderParticle(ParticleType<?> particle, Vec3 pos) {
        return shouldRender(
                PARTICLE_RENDERING_MODE.getOptionListValue(),
                particle,
                pos,
                particleType -> BuiltInRegistries.PARTICLE_TYPE.getId(particle),
                selectedParticleTypes,
                null
        );
    }
    private static <T> boolean shouldRender(
            SelectiveRenderingMode renderMode,
            T type,
            Vec3 pos,
            Function<T, Integer> getIdFunction,
            @Nullable
            List<Integer> selectedTypes,
            @Nullable
            List<BlockMatchRule> selectedBlockStates
    ) {
        if (renderMode == SelectiveRenderingMode.OFF) {
            return true;
        }
        boolean isSelected;
        if(selectedBlockStates == null){
            if(selectedTypes == null) return true;
            isSelected = isSelectedType(getIdFunction.apply(type), selectedTypes);
        }else{
            isSelected = isSelectedTypeAndState((BlockState) type, selectedBlockStates);
        }

        boolean isInArea = isSelectedArea(pos,selectedBlockStates != null);

        return switch (renderMode) {

            case INSIDE_SPECIFIC -> isInArea && isSelected;
            case INSIDE_NON_SPECIFIC -> isInArea && !isSelected;
            case INSIDE_ALL -> isInArea;

            case OUTSIDE_SPECIFIC -> !isInArea && isSelected;
            case OUTSIDE_NON_SPECIFIC -> !isInArea && !isSelected;
            case OUTSIDE_ALL -> !isInArea;

            case ANY_SPECIFIC -> isSelected;
            case ANY_NON_SPECIFIC -> !isSelected;
            default -> throw new IllegalStateException("Unexpected value: " + renderMode);
        };

    }
    public static boolean isSelectedType(int id, List<Integer> selectedTypes) {
        return selectedTypes.contains(id);
    }
    public static boolean isSelectedTypeAndState(BlockState state, List<BlockMatchRule> selectedTypes) {
        for (BlockMatchRule rule : selectedTypes) {
            if(rule.matches(state)) return true;
        }
        return false;
    }
    public static boolean isSelectedArea(Vec3 blockPos,boolean forBlockPos){
        for(AreaBox selectedArea : selectedAreas){
            if (isInsideArea(blockPos, selectedArea,forBlockPos )) {
                return true;
            }
        }
        return false;
    }
    public static boolean isInsideArea(Vec3 pos, AreaBox areaBox,boolean forBlockPos){
        float f = forBlockPos?0:1;
        return areaBox.minPos.getX() <= pos.x() && pos.x() <= areaBox.maxPos.getX()+f &&
                areaBox.minPos.getY() <= pos.y() && pos.y() <= areaBox.maxPos.getY()+f &&
                areaBox.minPos.getZ() <= pos.z() && pos.z() <= areaBox.maxPos.getZ()+f;
    }
    public static void scheduleChunkRebuild() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        if (lightUpdateTask != null) {
            lightUpdateTask.interrupt();
            lightUpdateTask = null;
        }

        client.levelRenderer.allChanged();

        if (!FORCE_LIGHT_UPDATE.getBooleanValue()) {
            return;
        }

        ClientLevel level = client.level;
        //? if >=1.21.3 {
        int worldTop = level.getMaxY() - 1;
        //?} else {
        /*int worldTop = level.getMaxBuildHeight() - 1;
        *///?}
        lightUpdateTask = new Thread(() -> {

            List<BlockPos> toUpdate = new ArrayList<>();

            for (AreaBox area : selectedAreas) {
                if (Thread.currentThread().isInterrupted()) return;

                BlockPos min = area.minPos;
                BlockPos max = area.maxPos;

                BlockPos outerMin = min.offset(-15, 0, -15);
                BlockPos outerMax = max.offset(15, 0, 15);

                BlockPos innerMin = min.offset(15, 0, 15);
                BlockPos innerMax = max.offset(-15, 0, -15);

                for (int x = outerMin.getX(); x <= outerMax.getX(); x++) {
                    for (int z = outerMin.getZ(); z <= outerMax.getZ(); z++) {

                        if (!isInShell(new BlockPos(x, 0, z),
                                outerMin, outerMax,
                                innerMin, innerMax)) {
                            continue;
                        }

                        for (int y = min.getY(); y <= worldTop; y++) {
                            toUpdate.add(new BlockPos(x, y, z));
                        }
                    }
                }
            }

            Iterator<BlockPos> it = toUpdate.iterator();

            level.queueLightUpdate(new Runnable() {
                @Override
                public void run() {
                    int limit = 2000;
                    LevelLightEngine engine = level.getLightEngine();

                    while (it.hasNext() && limit-- > 0) {
                        engine.checkBlock(it.next());
                    }

                    if (it.hasNext()) {
                        level.queueLightUpdate(this);
                    }
                }
            });

        }, "Lucidity-Light-Recalc");

        lightUpdateTask.start();
    }

    private static boolean isInShell(BlockPos pos,
                                       BlockPos outerMin, BlockPos outerMax,
                                       BlockPos innerMin, BlockPos innerMax) {

        if (pos.getX() < outerMin.getX() || pos.getX() > outerMax.getX()) return false;
        if (pos.getZ() < outerMin.getZ() || pos.getZ() > outerMax.getZ()) return false;
        if (innerMin.getX() > innerMax.getX()
                || innerMin.getZ() > innerMax.getZ()) {
            return true;
        }
        return pos.getX() < innerMin.getX() || pos.getX() > innerMax.getX()
                || pos.getZ() < innerMin.getZ() || pos.getZ() > innerMax.getZ();
    }

}