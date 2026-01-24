package ml.mypals.lucidity.datagen.lang;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class LucidityLanguageGenerator {
    public LucidityLanguageProviderManager languageProviderManager;
    private CompletableFuture<HolderLookup.Provider> registries;
    public LucidityLanguageGenerator(CompletableFuture<HolderLookup.Provider> registries) {
        languageProviderManager = new LucidityLanguageProviderManager(registries);
    }

    public void generateTranslations(FabricDataGenerator.Pack pack) {
        //=====DECLARE======
        languageProviderManager
                .declareProvider("en_us")
                .declareProvider("zh_cn");
        //======INFO========
        languageProviderManager.addTranslation("lucidity.info.singleplayer_only","Only accurate/usable in single-player","仅单人游戏准确/可用");
        languageProviderManager.addTranslation("lucidity.title.configs","Lucidity %s","我的视界 %s");
        //======TABS========
        languageProviderManager.addTranslation("lucidity.config_gui.other","Other","其它");
        languageProviderManager.addTranslation("lucidity.config_gui.explosion_visualizer","Explosion Visualizer","爆炸可视化");
        languageProviderManager.addTranslation("lucidity.config_gui.colors","Colors","颜色");
        languageProviderManager.addTranslation("lucidity.config_gui.generic","Generic","通用");
        languageProviderManager.addTranslation("lucidity.config_gui.features","Features","功能");
        languageProviderManager.addTranslation("lucidity.config_gui.selective_rendering","Selective Rendering","选择性渲染");
        languageProviderManager.addTranslation("lucidity.config_gui.image_rendering","ImageRendering","图片渲染");
        // ===== Generic Configs =====
        languageProviderManager.addTranslation(
                "lucidity.config.generic.name.open_config",
                "Open Config Screen",
                "打开配置界面"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.generic.comment.open_config",
                "Open the Lucidity configuration screen",
                "打开 Lucidity 的配置界面"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.other.name.yacl_like_config",
                "Use YACL-style Config Screen",
                "使用 YACL 风格的配置界面"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.other.comment.yacl_like_config",
                "Use a YACL-style layout for the configuration screen",
                "使用 YACL 风格的配置界面布局"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.generic.name.sculk_detect_range",
                "Sculk Detection Range",
                "幽匿探测范围"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.generic.comment.sculk_detect_range",
                "Maximum range for detecting sculk-related events",
                "检测幽匿相关事件的最大范围"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.generic.name.world_eater_helper_render_height",
                "World Eater Helper Render Height",
                "世界吞噬者助手渲染高度"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.generic.comment.world_eater_helper_render_height",
                "Vertical render height offset for the World Eater Helper (Due to technical limitations, the maximum supported value is 7.8).",
                "世界吞噬者助手渲染高度偏移（由于技术限制，最大仅支持 7.8）"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.generic.name.world_eater_helper_target",
                "World Eater Helper Detection Target",
                "世界吞噬者助手探测目标"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.generic.comment.world_eater_helper_target",
                """
                        Target blocks detected by the World Eater Helper. Supported expressions:
                        Block ID: a specific block
                        Block ID[block states]: a block matching the specified states
                        #Block Tag: any block with the specified tag
                        #Block Tag[block states]: any block with the specified tag and states
                        any / * / ? / %[block states]: any block matching the specified states""",
                """
                        世界吞噬者助手的探测目标。支持以下表达方式：
                        方块名：指定方块
                        方块名[方块状态]：符合指定方块状态的方块
                        #方块标签：属于指定标签的任意方块
                        #方块标签[方块状态]：属于指定标签且符合指定状态的方块
                        any / * / ? / %[方块状态]：任意符合指定状态的方块"""
        );

        // Fluid Transparency Override
        languageProviderManager.addTranslation("lucidity.config.generic.name.fluid_transparency", "Fluid Transparency", "流体透明度");
        languageProviderManager.addTranslation("lucidity.config.generic.comment.fluid_transparency", "Fluid transparency when Fluid Transparency Override is enabled", "覆盖流体渲染使用的透明度");

        // ===== Selective Rendering =====
        languageProviderManager.addTranslation(
                "category.selective_renderings",
                "Selective Renderings",
                "选择性渲染"
        );
        languageProviderManager.addTranslation(
                "key.selective_renderings.addSelection",
                "Add Selection Area",
                "添加选区"
        );
        languageProviderManager.addTranslation(
                "key.selective_renderings.renderingMode",
                "Switch Render Mode",
                "切换渲染模式"
        );
        languageProviderManager.addTranslation(
                "key.selective_renderings.removeSelection",
                "Remove Selection Area",
                "删除选区"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.name.wand",
                "Selection Wand",
                "选择魔杖"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.comment.wand",
                "Item used to select areas, blocks, entities, or particles",
                "用于选择区域、方块、实体或粒子的物品"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.name.force_light_update",
                "Force Light Update",
                "强制光照更新"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.comment.force_light_update",
                "Force light recalculation when selective rendering is applied",
                "在应用选择性渲染时强制重新计算光照"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.name.selected_areas",
                "Selected Areas",
                "已选择的区域"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.comment.selected_areas",
                "List of selected areas used for selective rendering",
                "用于选择性渲染的已选区域列表"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.name.selected_blocks",
                "Selected Blocks",
                "已选择的方块"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.comment.selected_blocks",
                "List of selected blocks used for selective rendering",
                "用于选择性渲染的已选方块列表"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.name.selected_entities",
                "Selected Entities",
                "已选择的实体"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.comment.selected_entities",
                "List of selected entities used for selective rendering",
                "用于选择性渲染的已选实体列表"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.name.selected_particles",
                "Selected Particles",
                "已选择的粒子"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.comment.selected_particles",
                "List of selected particles used for selective rendering",
                "用于选择性渲染的已选粒子列表"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.name.block_mode",
                "Block Rendering Mode",
                "方块渲染模式"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.comment.block_mode",
                "Control how blocks are selectively rendered",
                "控制方块的选择性渲染方式"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.name.entity_mode",
                "Entity Rendering Mode",
                "实体渲染模式"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.comment.entity_mode",
                "Control how entities are selectively rendered",
                "控制实体的选择性渲染方式"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.name.particle_mod",
                "Particle Rendering Mode",
                "粒子渲染模式"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.comment.particle_mod",
                "Control how particles are selectively rendered",
                "控制粒子的选择性渲染方式"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.name.apply_target",
                "Apply Target",
                "应用目标"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.selective_rendering.comment.apply_target",
                "Choose which targets selective rendering applies to",
                "选择选择性渲染所作用的目标"
        );
        // ===== Rendering Mode =====
        languageProviderManager.addTranslation(
                "config.lucidity.render_mode.off",
                "Off",
                "关闭"
        );

        languageProviderManager.addTranslation(
                "config.lucidity.render_mode.inside_specific",
                "Inside Area · Specific",
                "选区内 · 指定类型"
        );

        languageProviderManager.addTranslation(
                "config.lucidity.render_mode.inside_non_specific",
                "Inside Area · Non-specific",
                "选区内 · 非指定类型"
        );

        languageProviderManager.addTranslation(
                "config.lucidity.render_mode.inside_all",
                "Inside Area · All",
                "选区内 · 全部"
        );

        languageProviderManager.addTranslation(
                "config.lucidity.render_mode.outside_specific",
                "Outside Area · Specific",
                "选区外 · 指定类型"
        );

        languageProviderManager.addTranslation(
                "config.lucidity.render_mode.outside_non_specific",
                "Outside Area · Non-specific",
                "选区外 · 非指定类型"
        );

        languageProviderManager.addTranslation(
                "config.lucidity.render_mode.outside_all",
                "Outside Area · All",
                "选区外 · 全部"
        );

        languageProviderManager.addTranslation(
                "config.lucidity.render_mode.any_specific",
                "Any Area · Specific",
                "所有区域 · 指定类型"
        );

        languageProviderManager.addTranslation(
                "config.lucidity.render_mode.any_non_specific",
                "Any Area · Non-specific",
                "所有区域 · 非指定类型"
        );

        // ===== Wand HUD Tooltips =====

        languageProviderManager.addTranslation(
                "lucidity.info.wand.switchWandMode",
                "Switch wand mode",
                "切换魔杖模式"
        );

        languageProviderManager.addTranslation(
                "lucidity.info.wand.switchRenderingNext",
                "Next rendering mode",
                "下一个渲染模式"
        );

        languageProviderManager.addTranslation(
                "lucidity.info.wand.switchRenderingLast",
                "Previous rendering mode",
                "上一个渲染模式"
        );

        languageProviderManager.addTranslation(
                "lucidity.info.wand.addSpecific",
                "Add selected target",
                "添加选中的目标"
        );

        languageProviderManager.addTranslation(
                "lucidity.info.wand.addArea",
                "Add area",
                "添加区域"
        );

        languageProviderManager.addTranslation(
                "lucidity.info.wand.delete",
                "Delete area",
                "删除区域"
        );

        languageProviderManager.addTranslation(
                "lucidity.info.wand.cut",
                "Cut area",
                "剪切区域"
        );

        languageProviderManager.addTranslation(
                "lucidity.info.wand.selectP1",
                "Select first position",
                "选择第一个点"
        );

        languageProviderManager.addTranslation(
                "lucidity.info.wand.selectP2",
                "Select second position",
                "选择第二个点"
        );

        // ===== Wand Apply Target =====

        languageProviderManager.addTranslation(
                "lucidity.info.wand.apply_to_blocks",
                "Apply to Blocks",
                "作用于方块"
        );

        languageProviderManager.addTranslation(
                "lucidity.info.wand.apply_to_entities",
                "Apply to Entities",
                "作用于实体"
        );

        languageProviderManager.addTranslation(
                "lucidity.info.wand.apply_to_particles",
                "Apply to Particles",
                "作用于粒子"
        );
        // ===== Info =====
        languageProviderManager.addTranslation(
                "lucidity.info.cant_add_area",
                "Cannot add area",
                "无法添加区域"
        );
        languageProviderManager.addTranslation(
                "lucidity.info.add_type",
                "Added type: ",
                "已添加类型："
        );

        languageProviderManager.addTranslation(
                "lucidity.info.removed_type",
                "Removed type: ",
                "已移除类型："
        );
        languageProviderManager.addTranslation(
                "lucidity.info.clear",
                "Selection cleared",
                "已清除选择"
        );


        // ===== Color Configs =====

       languageProviderManager.addTranslation(
                "lucidity.config.colors.name.sculk_sensor_color",
                "Sculk Sensor Range Color",
                "幽匿传感器范围颜色"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.colors.comment.sculk_sensor_color",
                "Color used to render the sculk sensor detection range",
                "用于渲染幽匿传感器探测范围的颜色"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.colors.name.y_rot_color",
                "Y-axis Rotation Color",
                "Y 轴旋转颜色"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.colors.comment.y_rot_color",
                "Color used to visualize Y-axis rotation",
                "用于可视化 Y 轴旋转的颜色"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.colors.name.body_rot_color",
                "Body Rotation Color",
                "身体旋转颜色"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.colors.comment.body_rot_color",
                "Color used to visualize entity body rotation",
                "用于可视化实体身体旋转的颜色"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.colors.name.explosion_destruction_color",
                "Explosion Destruction Color",
                "爆炸破坏颜色"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.colors.comment.explosion_destruction_color",
                "Color for visualizing blocks destroyed by explosions",
                "用于可视化被爆炸破坏的方块的颜色"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.colors.name.explosion_center_color",
                "Explosion Center Color",
                "爆炸中心颜色"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.colors.comment.explosion_center_color",
                "Color for the explosion center point marker",
                "爆炸中心点标记的颜色"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.colors.name.sample_point_safe_color",
                "Sample Point Safe Color",
                "安全采样点颜色"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.colors.comment.sample_point_safe_color",
                "Color for entity sample points protected from explosion damage",
                "受保护免受爆炸伤害的实体采样点的颜色"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.colors.name.sample_point_exposed_color",
                "Sample Point Exposed Color",
                "暴露采样点颜色"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.colors.comment.sample_point_exposed_color",
                "Color for entity sample points exposed to explosion damage",
                "暴露于爆炸伤害的实体采样点的颜色"
        );

// Creaking Heart Indicator Color
        languageProviderManager.addTranslation("lucidity.config.colors.name.creaking_heart_indicator_color", "Creaking Heart Indicator Color", "嘎枝之心指示器颜色");
        languageProviderManager.addTranslation("lucidity.config.colors.comment.creaking_heart_indicator_color", "Color for the creaking heart indicator overlay", "嘎枝之心指示器覆盖层的颜色");

// Item Merge Range Color
        languageProviderManager.addTranslation("lucidity.config.colors.name.item_merg_range_visualize_color", "Item Merge Range Color", "物品合并范围颜色");
        languageProviderManager.addTranslation("lucidity.config.colors.comment.item_merg_range_visualize_color", "Color for visualizing item merge range", "物品合并范围可视化的颜色");

// Wither Destruction Range Color
        languageProviderManager.addTranslation("lucidity.config.colors.name.wither_destruction_range_color", "Wither Destruction Range Color", "凋零破坏范围颜色");
        languageProviderManager.addTranslation("lucidity.config.colors.comment.wither_destruction_range_color", "Color for the wither's destruction range visualization", "凋零破坏范围可视化的颜色");

// Ender Dragon Destruction Range Color
        languageProviderManager.addTranslation("lucidity.config.colors.name.ender_dragon_destruction_color", "Ender Dragon Destruction Range Color", "末影龙破坏范围颜色");
        languageProviderManager.addTranslation("lucidity.config.colors.comment.ender_dragon_destruction_color", "Color for the ender dragon's destruction range visualization", "末影龙破坏范围可视化的颜色");

// Ender Dragon Waypoint Color
        languageProviderManager.addTranslation("lucidity.config.colors.name.ender_dragon_waypoint_color", "Ender Dragon Waypoint Color", "末影龙路径点颜色");
        languageProviderManager.addTranslation("lucidity.config.colors.comment.ender_dragon_waypoint_color", "Color for ender dragon waypoint markers", "末影龙路径点标记的颜色");

        // ===== features =====
        languageProviderManager.addTranslation(
                "lucidity.config.features.name.container_signal_preview",
                "Container Signal Preview",
                "容器信号预览"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.container_signal_preview",
                "Preview redstone signal strength from containers",
                "预览容器的红石信号强度"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.extended_hitbox_visualize",
                "Extended Hitbox Visualize",
                "扩展碰撞箱可视化"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.extended_hitbox_visualize",
                "Visualize extended hitboxes of entities",
                "可视化实体的扩展碰撞箱"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.name.sculk_sensor_range",
                "Sculk Sensor Range",
                "幽匿传感器范围"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.sculk_sensor_range",
                "Visualize the detection range of sculk sensors",
                "可视化幽匿传感器的探测范围"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.depth_map_overlay",
                "Depth map Overlay",
                "深度图覆盖"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.depth_map_overlay",
                "Render depth_map on screen",
                "渲染深度图"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.vibration_trace",
                "Vibration Trace",
                "震动传播可视化"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.vibration_trace",
                "Show vibration propagation paths",
                "展示震动的传播路径"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.zalgo_text_deobfuscate",
                "Text Deobfuscation",
                "& k乱码文本去混淆"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.zalgo_text_deobfuscate",
                "Removes visual obfuscation effects from & k text",
                "移除& k乱码文本的视觉混淆效果"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.falling_block_preview",
                "Falling Block Preview",
                "下落方块预览"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.falling_block_preview",
                "Previews the landing position of falling blocks",
                "预览下落方块的最终落点位置"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.sound_visualize",
                "Sound Visualize",
                "声音可视化"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.sound_visualize",
                "Visualizes sound events in the world",
                "将游戏中的声音事件以可视化方式显示"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.block_event_visualize",
                "Block Event Visualize",
                "方块事件可视化"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.block_event_visualize",
                "Displays block-related events visually",
                "以可视化形式显示方块相关事件"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.void_alarm",
                "Void Alarm",
                "虚空警报"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.void_alarm",
                "Render red glass on void",
                "在虚空上渲染红色玻璃"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.mob_spawn_visualize",
                "Mob Spawn Visualize",
                "生物生成可视化"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.mob_spawn_visualize",
                "Visualizes mob spawn",
                "可视化生物生成游走"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.invisible_entity_transparency_override",
                "Invisible Entity Transparency Override",
                "隐形实体透明度覆盖"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.invisible_entity_transparency_override",
                "Overrides the transparency of invisible entities to make them visible",
                "覆盖隐形实体的透明度，使其可见"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.carpet_tis_microtiming_visual_enhance",
                "Microtiming Marker Visual Enhancement",
                "微时序标记视觉增强"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.carpet_tis_microtiming_visual_enhance",
                "Enhances the visual display of Carpet TIS microtiming markers when mouse hovered on \"#\" in chat",
                "鼠标悬浮于输出的\"#\"时增强 Carpet TIS 微时序标记的可视化显示"
        );


        languageProviderManager.addTranslation(
                "lucidity.config.features.name.boat_restriction",
                "Boat View Restriction",
                "船只视角限制"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.boat_restriction",
                "Visualize view restrictions while riding a boat",
                "可视化乘坐船只时的视角限制"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.body_yaw",
                "Body Yaw",
                "身体朝向"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.body_yaw",
                "Visualize entity body yaw direction",
                "可视化实体身体的朝向角度"
        );

        // Fluid Transparency Override
        languageProviderManager.addTranslation("lucidity.config.features.name.fluid_transparency_override", "Fluid Transparency Override", "流体透明度覆盖");
        languageProviderManager.addTranslation("lucidity.config.features.comment.fluid_transparency_override", "Override fluid rendering to make it transparent", "覆盖流体渲染使其透明");

// Creaking Heart Indicator
        languageProviderManager.addTranslation("lucidity.config.features.name.creaking_heart_indicator", "Creaking Heart Indicator", "嘎枝之心指示器");
        languageProviderManager.addTranslation("lucidity.config.features.comment.creaking_heart_indicator", "Display indicator for creaking heart blocks", "显示嘎枝之心方块的指示器");

// Item Merge Range Visualize
        languageProviderManager.addTranslation("lucidity.config.features.name.item_merg_range_visualize", "Item Merge Range Visualizer", "物品合并范围可视化");
        languageProviderManager.addTranslation("lucidity.config.features.comment.item_merg_range_visualize", "Visualize the range within which items can merge", "可视化物品可以合并的范围");

// Ender Dragon Waypoints Visualize
        languageProviderManager.addTranslation("lucidity.config.features.name.ender_dragon_waypoints_visualize", "Ender Dragon Waypoints Visualizer", "末影龙路径点可视化");
        languageProviderManager.addTranslation("lucidity.config.features.comment.ender_dragon_waypoints_visualize", "Visualize ender dragon flight waypoints", "可视化末影龙的飞行路径点");

// Ender Dragon Destruction Visualize
        languageProviderManager.addTranslation("lucidity.config.features.name.ender_dragon_destruction_visualize", "Ender Dragon Destruction Visualizer", "末影龙破坏范围可视化");
        languageProviderManager.addTranslation("lucidity.config.features.comment.ender_dragon_destruction_visualize", "Visualize the ender dragon's block destruction range", "可视化末影龙的方块破坏范围");

// Wither Destruction Visualize
        languageProviderManager.addTranslation("lucidity.config.features.name.wither_destruction_visualize", "Wither Destruction Visualizer", "凋零破坏范围可视化");
        languageProviderManager.addTranslation("lucidity.config.features.comment.wither_destruction_visualize", "Visualize the wither's block destruction range", "可视化凋零的方块破坏范围");

// Nether Coordinate Calculator
        languageProviderManager.addTranslation("lucidity.config.features.name.coordinate_caculator", "Nether Coordinate Calculator", "下界坐标计算器");
        languageProviderManager.addTranslation("lucidity.config.features.comment.coordinate_caculator", "Calculate and display corresponding coordinates between Overworld and Nether", "计算并显示主世界与下界之间的对应坐标");

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.world_eater_mine_helper",
                "World Eater Mine Helper",
                "世吞助手"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.world_eater_mine_helper",
                "Renders additional models above specific blocks to make them more noticeable. Inspired by the OMMC mod, but more powerful.",
                "在特定方块的上方渲染额外模型使其变得显眼，源自 OMMC 模组，但更加强大"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.vault_item_display",
                "Trial Vault Content Highlighter",
                "显眼试试炼宝库内容"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.vault_item_display",
                "Highlights the content inside trial spawners and vaults, making it easier to see.",
                "让试炼宝库与试炼笼中的内容更加显眼"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.block_no_random_offset",
                "Disable Block Random Offset",
                "禁用方块随机偏移"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.block_no_random_offset",
                "Disables random positional offsets applied to certain block models",
                "禁用部分方块模型应用的随机位置偏移"
        );


        languageProviderManager.addTranslation(
                "lucidity.config.features.name.accurate_entity_shadow",
                "Better Entity Shadow",
                "更好的实体阴影"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.accurate_entity_shadow",
                "Make entity shadow slightly better.",
                "让实体阴影稍微好一点"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.b36_preview",
                "B36 Destination Preview",
                "b36目的地预览"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.b36_preview",
                "Preview b36 destination.",
                "预览b36地目的地"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.features.name.fluid_source_highlight",
                "Fluid Source Highlight",
                "流体源高亮"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.features.comment.fluid_source_highlight",
                "Allows fluid source to use custom textures.",
                "允许流体源使用自定义材质"
        );

// Explosion Visualizer - 爆炸可视化
        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.name.explode_timer",
                "Explosion Timer",
                "爆炸计时器"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.comment.explode_timer",
                "Display explosion timer information",
                "显示爆炸计时信息"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.name.main_render",
                "Enable Explosion Visualizer",
                "启用爆炸可视化"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.comment.main_render",
                "Main toggle for explosion visualization rendering",
                "爆炸可视化渲染的主开关"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.name.explosion_center",
                "Explosion Center",
                "爆炸中心"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.comment.explosion_center",
                "Display the center point of explosions",
                "显示爆炸的中心点"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.name.block_destruction",
                "Block Destruction",
                "方块破坏"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.comment.block_destruction",
                "Visualize blocks affected by explosion destruction",
                "可视化受爆炸破坏影响的方块"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.name.block_ray_cast",
                "Block Ray Cast",
                "方块射线投射"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.comment.block_ray_cast",
                "Display ray casting lines from explosion to blocks",
                "显示从爆炸到方块的射线投射线"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.name.entity_sample_points",
                "Entity Sample Points",
                "实体采样点"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.comment.entity_sample_points",
                "Show sample points used for entity explosion damage calculation",
                "显示用于实体爆炸伤害计算的采样点"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.name.sample_point_ray_cast",
                "Sample Point Ray Cast",
                "采样点射线投射"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.explosion_visualizer.comment.sample_point_ray_cast",
                "Display ray casting from entity sample points to explosion",
                "显示从实体采样点到爆炸的射线投射"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.image_rendering.name.image_rendering",
                "Image Rendering",
                "图像渲染"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.image_rendering.comment.image_rendering",
                "Enables rendering images in the world using blocks",
                "启用使用方块在世界中渲染图像"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.image_rendering.name.images",
                "Images",
                "图像列表"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.image_rendering.comment.images",
                "List of images to be rendered \n(<image_source(Path/URL)>;<image_id>;<position(x,y,z)>;<rotation(x,y,z)>;<scale(x,y)>)",
                "需要渲染的图像列表 \n例:(<image_source(Path/URL)>;<image_id>;<position(x,y,z)>;<rotation(x,y,z)>;<scale(x,y)>)"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.image_rendering.name.pixels_per_block",
                "Pixels per Block",
                "每方块像素数"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.image_rendering.comment.pixels_per_block",
                "Number of image pixels represented by a single block",
                "单个方块所表示的图像像素数量"
        );

        languageProviderManager.addTranslation(
                "lucidity.config.generic.name.invisible_entity_alpha",
                "Invisible Entity Transparency",
                "隐形实体透明度"
        );
        languageProviderManager.addTranslation(
                "lucidity.config.generic.comment.invisible_entity_alpha",
                "Transparency level used when rendering invisible entities (0.0 = fully invisible, 1.0 = fully visible)",
                "渲染隐形实体时使用的透明度（0.0 为完全不可见，1.0 为完全可见）"
        );

        //======SUBMIT========
        languageProviderManager.submit(pack);
    }
}
