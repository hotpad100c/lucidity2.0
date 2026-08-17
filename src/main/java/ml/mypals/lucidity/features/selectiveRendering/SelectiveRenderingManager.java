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
        // 选区形状没变时只需要重新提交渲染用的 Shape，不必碰区块网格
        if (unchanged) {
            return;
        }
        onSelectedAreasChanged(before, regionsOf(selectedAreas));
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
    // ------------------------------------------------------------------
    // 区块重建调度
    //
    // 一次配置变化只需要重建 shouldRenderBlock 结果真正发生改变的那些区段。
    // 依据 shouldRender 的判定表：
    //   INSIDE_*  = isInArea && f(isSelected)  —— 选区外恒为"隐藏"
    //   OUTSIDE_* = !isInArea && f(isSelected) —— 选区内恒为"隐藏"
    //   ANY_*     结果与 isInArea 无关
    //   *_ALL     结果与 isSelected 无关
    // 由此可以判断某次变化是否被关在选区内。
    // ------------------------------------------------------------------

    /** 一个闭区间方块范围。 */
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

    /** 邻居面剔除、AO 以及活塞方块实体最多向外读 1 格，受影响范围要相应外扩。 */
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

    /** isInArea 是否参与该模式的判定。 */
    private static boolean usesArea(SelectiveRenderingMode mode) {
        return switch (mode) {
            case INSIDE_SPECIFIC, INSIDE_NON_SPECIFIC, INSIDE_ALL,
                 OUTSIDE_SPECIFIC, OUTSIDE_NON_SPECIFIC, OUTSIDE_ALL -> true;
            case OFF, ANY_SPECIFIC, ANY_NON_SPECIFIC -> false;
        };
    }

    /** 方块类型/状态过滤是否参与该模式的判定。 */
    private static boolean usesTypeFilter(SelectiveRenderingMode mode) {
        return switch (mode) {
            case INSIDE_SPECIFIC, INSIDE_NON_SPECIFIC,
                 OUTSIDE_SPECIFIC, OUTSIDE_NON_SPECIFIC,
                 ANY_SPECIFIC, ANY_NON_SPECIFIC -> true;
            case OFF, INSIDE_ALL, OUTSIDE_ALL -> false;
        };
    }

    /** 该模式下选区外的方块恒被判为隐藏，因此任何变化都被关在选区里。 */
    private static boolean allHiddenOutsideArea(SelectiveRenderingMode mode) {
        return mode == SelectiveRenderingMode.INSIDE_SPECIFIC
                || mode == SelectiveRenderingMode.INSIDE_NON_SPECIFIC
                || mode == SelectiveRenderingMode.INSIDE_ALL;
    }

    /** 选区增删改。 */
    public static void onSelectedAreasChanged(List<BlockRegion> before, List<BlockRegion> after) {
        List<BlockRegion> touched = new ArrayList<>(before.size() + after.size());
        touched.addAll(before);
        touched.addAll(after);

        SelectiveRenderingMode mode = BLOCK_RENDERING_MODE.getOptionListValue();
        // ANY_* / OFF 下 isInArea 根本不参与判定，选区怎么改都不影响方块可见性
        applyRebuild(usesArea(mode) ? touched : List.of(), touched);
    }

    /** 选中方块类型/状态列表变化。 */
    public static void onSelectedBlockTypesChanged() {
        SelectiveRenderingMode mode = BLOCK_RENDERING_MODE.getOptionListValue();
        List<BlockRegion> areas = regionsOf(selectedAreas);

        if (!usesTypeFilter(mode)) {
            // OFF / *_ALL：类型过滤不参与判定
            applyRebuild(List.of(), areas);
        } else if (allHiddenOutsideArea(mode)) {
            // INSIDE_*（"只显示选区内的某些方块，其余透明"就在这里）：
            // 选区外恒为隐藏，与选了哪些类型无关，所以变化只可能发生在选区内
            applyRebuild(areas, areas);
        } else {
            // OUTSIDE_* / ANY_*：被选中的类型可能出现在世界任何角落
            applyRebuild(null, areas);
        }
    }

    /** 方块渲染模式切换。 */
    public static void onBlockRenderModeChanged(SelectiveRenderingMode before, SelectiveRenderingMode after) {
        List<BlockRegion> areas = regionsOf(selectedAreas);

        if (before == after) {
            applyRebuild(List.of(), areas);
        } else if (allHiddenOutsideArea(before) && allHiddenOutsideArea(after)) {
            // 两个模式都把选区外判为隐藏，差异只可能出现在选区内
            applyRebuild(areas, areas);
        } else {
            applyRebuild(null, areas);
        }
    }

    /**
     * 隐藏方块透明度变化。
     *
     * 实体/方块实体走 uniform，改透明度对它们完全不需要重建；这里要重建纯粹是因为
     * 地形三条后端（原版 / fabric indigo / sodium）仍然把 alpha 烘焙进顶点色。
     * 所以只需要重建"含有隐藏方块"的那些区段。
     *
     * 透明度不改变哪些方块被隐藏，只改变它们的颜色，因此不触发光照重算。
     */
    public static void onHiddenTransparencyChanged() {
        SelectiveRenderingMode mode = BLOCK_RENDERING_MODE.getOptionListValue();

        if (mode == SelectiveRenderingMode.OFF) {
            // 没有任何方块被判为隐藏
            applyRebuild(List.of(), List.of());
        } else if (mode == SelectiveRenderingMode.OUTSIDE_ALL) {
            // 唯一一个"隐藏集合完全落在选区内"的模式（隐藏 == isInArea）
            applyRebuild(regionsOf(selectedAreas), List.of());
        } else {
            // 其余模式下选区外也存在隐藏方块，只能全量
            applyRebuild(null, List.of());
        }
    }

    /** 保守兜底：全量重建。 */
    public static void scheduleChunkRebuild() {
        applyRebuild(null, regionsOf(selectedAreas));
    }

    /**
     * @param dirty        需要重建的范围；{@code null} 表示整个世界，空列表表示无需重建
     * @param lightRegions 需要重算光照的范围
     */
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
            // 可见性完全没变，连光照都不用动
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

        // 选区可以画得非常大，按视距裁剪，避免在根本加载不到的区段坐标上空转
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
                        // 视野外的区段坐标会被 ViewArea 忽略，这里不需要额外判断。
                        // sodium 覆写了私有的 setSectionDirty(int,int,int,boolean)，
                        // 公开的三参重载会委托过去，所以两条管线都吃这个调用。
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