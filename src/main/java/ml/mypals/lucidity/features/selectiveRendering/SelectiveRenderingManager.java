package ml.mypals.lucidity.features.selectiveRendering;

import com.mojang.blaze3d.vertex.VertexFormat;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import ml.mypals.lucidity.Lucidity;
import ml.mypals.lucidity.utils.BlockMatchRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
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
        boolean unchanged = blockStrings.equals(lastBlockRuleStrings);
        lastBlockRuleStrings = List.copyOf(blockStrings);

        selectedBlockTypes.clear();
        for (String raw : blockStrings) {
            String input = raw.replace(" ", "").toLowerCase();
            try {
                selectedBlockTypes.add(parseRule(input));
            } catch (Exception e) {
                System.err.println("[Lucidity] Failed to parse rule: " + raw);
            }
        }
        // malilib 每次保存配置都会回调，列表没变就没有任何东西需要重建
        if (unchanged) {
            return;
        }
        onSelectedBlockTypesChanged();
    }

    public static void resolveSelectedEntityTypesFromString(List<String> entityStrings){
        selectedEntityTypes.clear();

        entityStrings.forEach(entityString -> {
            try {
                if (!entityString.contains(":")) {
                    entityString = "minecraft:" + entityString;
                }
                Identifier entityId = Identifier.tryParse(entityString);
                EntityType<?> targetEntity = BuiltInRegistries.ENTITY_TYPE.getValue(entityId);
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
                Identifier particleId = Identifier.tryParse(particleString);
                ParticleType<?> targetParticle = BuiltInRegistries.PARTICLE_TYPE.getValue(particleId);
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
            Identifier id = Identifier.tryParse(name);
            wand = BuiltInRegistries.ITEM.getValue(id);
        }catch (Exception e) {
            name = "minecraft:breeze_rod";
            System.err.println("Failed to parse wand item: " + name);
            wand = last_wind;
        }
    }
    public static void resolveSelectedAreasFromString(List<String> areaStrings){List<AreaBox> newAreas = new ArrayList<>();

        boolean unchanged = areaStrings.equals(lastAreaStrings);
        // 只有末尾的透明度段变了：选区形状没动，可见性判定不变，只有颜色需要刷新
        boolean transparencyOnly = !unchanged
                && lastAreaStrings != null
                && geometryOf(areaStrings).equals(geometryOf(lastAreaStrings));
        lastAreaStrings = List.copyOf(areaStrings);
        List<BlockRegion> before = regionsOf(selectedAreas);

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
        refreshOwnTransparencyFlag();
        // 选区形状没变时只需要重新提交渲染用的 Shape，不必碰区块网格
        if (unchanged) {
            return;
        }
        onSelectedAreasChanged(before, regionsOf(selectedAreas), transparencyOnly);
    }

    private static List<String> geometryOf(List<String> areaStrings) {
        List<String> result = new ArrayList<>(areaStrings.size());
        for (String areaString : areaStrings) {
            String[] parts = areaString.split(":");
            result.add(parts.length >= 2 ? parts[0].trim() + ":" + parts[1].trim() : areaString);
        }
        return result;
    }

    private static AreaBox parseAABB(String areaString) throws IllegalArgumentException {
        String[] parts = areaString.split(":");
        if (parts.length != 2 && parts.length != 3) {
            throw new IllegalArgumentException("Invalid format. Expected x1,y1,z1:x2,y2,z2[:transparency]");
        }

        int ownTransparency = AreaBox.FOLLOW_GLOBAL;
        if (parts.length == 3) {
            String raw = parts[2].trim();
            if (!raw.isEmpty()) {
                try {
                    ownTransparency = Math.clamp(Integer.parseInt(raw), 0, 255);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid transparency in input: " + areaString, e);
                }
            }
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
            ).withOwnTransparency(ownTransparency);
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
    // ------------------------------------------------------------------
    // 隐藏透明度的归属
    //
    // 每个选区默认跟随全局的 HIDDEN_BLOCK_TRANSPARENCY，也可以带一个自己的值
    // （配置字符串第三段，见 AreaBox#asString）。某个位置落在多个带自定义透明度的
    // 选区里时，取列表里第一个 —— 和 SELECTED_AREAS 的书写顺序一致，可预期。
    //
    // 实体/方块实体的透明度是绘制时通过 uniform 施加的，一批只能有一个值，所以
    // 那条路径不直接传数值，而是传一个"透明度来源"的槽位号，绘制时再解析成当前值。
    // 槽位按选区对角线（AreaBox#getKey）分配，这样关键帧点名的是选区本身，
    // 增删其它选区不会让引用错位。
    // ------------------------------------------------------------------
    public static final int GLOBAL_TRANSPARENCY_SOURCE = -1;
    private static final int MAX_TRANSPARENCY_SOURCES = 64;
    private static final Map<String, Integer> TRANSPARENCY_SOURCES = new ConcurrentHashMap<>();
    private static final Map<Integer, String> TRANSPARENCY_SOURCE_KEYS = new ConcurrentHashMap<>();
    private static final Queue<Integer> FREE_TRANSPARENCY_SOURCES = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger NEXT_TRANSPARENCY_SOURCE = new AtomicInteger();

    private static volatile boolean anyOwnTransparency = false;

    static void refreshOwnTransparencyFlag() {
        Set<String> live = new HashSet<>();
        for (AreaBox area : selectedAreas) {
            if (area.hasOwnTransparency()) {
                live.add(area.getKey());
            }
        }
        anyOwnTransparency = !live.isEmpty();
        TRANSPARENCY_SOURCES.entrySet().removeIf(entry -> {
            if (live.contains(entry.getKey())) {
                return false;
            }
            TRANSPARENCY_SOURCE_KEYS.remove(entry.getValue(), entry.getKey());
            FREE_TRANSPARENCY_SOURCES.offer(entry.getValue());
            return true;
        });
    }

    public static int globalHiddenTransparency() {
        return HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue();
    }
    @Nullable
    public static AreaBox findArea(String key) {
        if (key == null) {
            return null;
        }
        for (AreaBox area : selectedAreas) {
            if (area.getKey().equals(key)) {
                return area;
            }
        }
        return null;
    }

    private static int sourceOfArea(AreaBox area) {
        String key = area.getKey();
        Integer existing = TRANSPARENCY_SOURCES.get(key);
        if (existing != null) {
            return existing;
        }

        Integer assigned = TRANSPARENCY_SOURCES.computeIfAbsent(key, k -> {
            Integer recycled = FREE_TRANSPARENCY_SOURCES.poll();
            if (recycled != null) {
                return recycled;
            }
            int next = NEXT_TRANSPARENCY_SOURCE.getAndIncrement();
            return next < MAX_TRANSPARENCY_SOURCES ? next : GLOBAL_TRANSPARENCY_SOURCE;
        });
        if (assigned != GLOBAL_TRANSPARENCY_SOURCE) {
            TRANSPARENCY_SOURCE_KEYS.put(assigned, key);
        }
        return assigned;
    }

    public static int transparencySourceAt(Vec3 pos, boolean forBlockPos) {
        if (!anyOwnTransparency) {
            return GLOBAL_TRANSPARENCY_SOURCE;
        }
        for (AreaBox area : selectedAreas) {
            if (area.hasOwnTransparency() && isInsideArea(pos, area, forBlockPos)) {
                return sourceOfArea(area);
            }
        }
        return GLOBAL_TRANSPARENCY_SOURCE;
    }

    public static int transparencyOfSource(int source) {
        if (source != GLOBAL_TRANSPARENCY_SOURCE) {
            String key = TRANSPARENCY_SOURCE_KEYS.get(source);
            AreaBox area = key == null ? null : findArea(key);
            if (area != null && area.hasOwnTransparency()) {
                return area.getOwnTransparency();
            }
        }
        return globalHiddenTransparency();
    }

    public static int hiddenTransparencyAt(Vec3 pos, boolean forBlockPos) {
        if (!anyOwnTransparency) {
            return globalHiddenTransparency();
        }
        for (AreaBox area : selectedAreas) {
            if (area.hasOwnTransparency() && isInsideArea(pos, area, forBlockPos)) {
                return area.getOwnTransparency();
            }
        }
        return globalHiddenTransparency();
    }

    public static int hiddenTransparencyAt(BlockPos pos) {
        return hiddenTransparencyAt(new Vec3(pos.getX(), pos.getY(), pos.getZ()), true);
    }


    public static boolean isFullyTransparentAt(Vec3 pos, boolean forBlockPos) {
        return hiddenTransparencyAt(pos, forBlockPos) == 0;
    }

    public static boolean isFullyTransparentAt(BlockPos pos) {
        return hiddenTransparencyAt(pos) == 0;
    }

    public static boolean shouldSkipBlockGeometry(BlockState state, BlockPos pos) {
        return !shouldRenderBlock(state, pos) && isFullyTransparentAt(pos);
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
    public record BlockRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public static BlockRegion of(AreaBox area) {
            return new BlockRegion(
                    area.minPos.getX(), area.minPos.getY(), area.minPos.getZ(),
                    area.maxPos.getX(), area.maxPos.getY(), area.maxPos.getZ());
        }
        public BlockRegion expand(int n) {
            return new BlockRegion(minX - n, minY - n, minZ - n, maxX + n, maxY + n, maxZ + n);
        }
    }

    private static final int REGION_MARGIN = 1;

    private static List<String> lastAreaStrings = null;
    private static List<String> lastBlockRuleStrings = null;

    private static List<BlockRegion> regionsOf(Collection<AreaBox> areas) {
        List<BlockRegion> regions = new ArrayList<>(areas.size());
        for (AreaBox area : areas) {
            regions.add(BlockRegion.of(area));
        }
        return regions;
    }

    private static boolean usesArea(SelectiveRenderingMode mode) {
        return switch (mode) {
            case INSIDE_SPECIFIC, INSIDE_NON_SPECIFIC, INSIDE_ALL,
                 OUTSIDE_SPECIFIC, OUTSIDE_NON_SPECIFIC, OUTSIDE_ALL -> true;
            case OFF, ANY_SPECIFIC, ANY_NON_SPECIFIC -> false;
        };
    }

    private static boolean usesTypeFilter(SelectiveRenderingMode mode) {
        return switch (mode) {
            case INSIDE_SPECIFIC, INSIDE_NON_SPECIFIC,
                 OUTSIDE_SPECIFIC, OUTSIDE_NON_SPECIFIC,
                 ANY_SPECIFIC, ANY_NON_SPECIFIC -> true;
            case OFF, INSIDE_ALL, OUTSIDE_ALL -> false;
        };
    }

    private static boolean allHiddenOutsideArea(SelectiveRenderingMode mode) {
        return mode == SelectiveRenderingMode.INSIDE_SPECIFIC
                || mode == SelectiveRenderingMode.INSIDE_NON_SPECIFIC
                || mode == SelectiveRenderingMode.INSIDE_ALL;
    }

    public static void onSelectedAreasChanged(List<BlockRegion> before, List<BlockRegion> after) {
        onSelectedAreasChanged(before, after, false);
    }

    public static void onSelectedAreasChanged(List<BlockRegion> before, List<BlockRegion> after, boolean transparencyOnly) {
        List<BlockRegion> touched = new ArrayList<>(before.size() + after.size());
        touched.addAll(before);
        touched.addAll(after);

        if (transparencyOnly) {
            applyRebuild(touched, List.of());
            return;
        }

        SelectiveRenderingMode mode = BLOCK_RENDERING_MODE.getOptionListValue();
        applyRebuild(usesArea(mode) ? touched : List.of(), touched);
    }

    public static void onAreaTransparencyChanged(AreaBox area) {
        refreshOwnTransparencyFlag();
        if (BLOCK_RENDERING_MODE.getOptionListValue() == SelectiveRenderingMode.OFF) {
            return;
        }
        applyRebuild(List.of(BlockRegion.of(area)), List.of());
    }

    public static void onSelectedBlockTypesChanged() {
        SelectiveRenderingMode mode = BLOCK_RENDERING_MODE.getOptionListValue();
        List<BlockRegion> areas = regionsOf(selectedAreas);

        if (!usesTypeFilter(mode)) {
            applyRebuild(List.of(), areas);
        } else if (allHiddenOutsideArea(mode)) {
            applyRebuild(areas, areas);
        } else {
            applyRebuild(null, areas);
        }
    }

    public static void onBlockRenderModeChanged(SelectiveRenderingMode before, SelectiveRenderingMode after) {
        List<BlockRegion> areas = regionsOf(selectedAreas);

        if (before == after) {
            applyRebuild(List.of(), areas);
        } else if (allHiddenOutsideArea(before) && allHiddenOutsideArea(after)) {
            applyRebuild(areas, areas);
        } else {
            applyRebuild(null, areas);
        }
    }

    public static void onHiddenTransparencyChanged() {
        SelectiveRenderingMode mode = BLOCK_RENDERING_MODE.getOptionListValue();

        if (mode == SelectiveRenderingMode.OFF) {
            applyRebuild(List.of(), List.of());
        } else if (mode == SelectiveRenderingMode.OUTSIDE_ALL) {
            applyRebuild(regionsOf(selectedAreas), List.of());
        } else {
            applyRebuild(null, List.of());
        }
    }

    public static void scheduleChunkRebuild() {
        applyRebuild(null, regionsOf(selectedAreas));
    }

    private static void applyRebuild(@Nullable List<BlockRegion> dirty, List<BlockRegion> lightRegions) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        if (lightUpdateTask != null) {
            lightUpdateTask.interrupt();
            lightUpdateTask = null;
        }

        if (dirty == null) {
            client.levelRenderer.allChanged();
        } else if (!dirty.isEmpty()) {
            markSectionsDirty(client, dirty);
        } else {
            return;
        }

        if (!FORCE_LIGHT_UPDATE.getBooleanValue() || lightRegions.isEmpty()) {
            return;
        }
        startLightRecalc(client.level, lightRegions);
    }

    private static void markSectionsDirty(Minecraft client, List<BlockRegion> regions) {
        ClientLevel level = client.level;
        if (level == null) return;

        int levelMinSection = SectionPos.blockToSectionCoord(level.getMinY());
        int levelMaxSection = SectionPos.blockToSectionCoord(level.getMaxY());

        boolean clamp = client.player != null;
        int viewSections = client.options.getEffectiveRenderDistance() + 2;
        int camSectionX = clamp ? SectionPos.blockToSectionCoord(client.player.getBlockX()) : 0;
        int camSectionZ = clamp ? SectionPos.blockToSectionCoord(client.player.getBlockZ()) : 0;

        for (BlockRegion raw : regions) {
            BlockRegion region = raw.expand(REGION_MARGIN);

            int minSx = SectionPos.blockToSectionCoord(region.minX());
            int maxSx = SectionPos.blockToSectionCoord(region.maxX());
            int minSy = Math.max(levelMinSection, SectionPos.blockToSectionCoord(region.minY()));
            int maxSy = Math.min(levelMaxSection, SectionPos.blockToSectionCoord(region.maxY()));
            int minSz = SectionPos.blockToSectionCoord(region.minZ());
            int maxSz = SectionPos.blockToSectionCoord(region.maxZ());

            if (clamp) {
                minSx = Math.max(minSx, camSectionX - viewSections);
                maxSx = Math.min(maxSx, camSectionX + viewSections);
                minSz = Math.max(minSz, camSectionZ - viewSections);
                maxSz = Math.min(maxSz, camSectionZ + viewSections);
            }

            for (int sx = minSx; sx <= maxSx; sx++) {
                for (int sy = minSy; sy <= maxSy; sy++) {
                    for (int sz = minSz; sz <= maxSz; sz++) {
                        client.levelRenderer.setSectionDirty(sx, sy, sz);
                    }
                }
            }
        }
    }

    private static void startLightRecalc(ClientLevel level, List<BlockRegion> regions) {
        int worldTop = level.getMaxY() - 1;
        lightUpdateTask = new Thread(() -> {

            List<BlockPos> toUpdate = new ArrayList<>();

            for (BlockRegion region : regions) {
                if (Thread.currentThread().isInterrupted()) return;

                BlockPos min = new BlockPos(region.minX(), region.minY(), region.minZ());
                BlockPos max = new BlockPos(region.maxX(), region.maxY(), region.maxZ());

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