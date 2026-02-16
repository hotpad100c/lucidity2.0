package ml.mypals.lucidity.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record BlockMatchRule(
        @Nullable Block block,
        @Nullable TagKey<Block> tag,
        Map<Property<?>, String> states
) {


    public boolean matches(BlockState state) {

        if (block != null && state.getBlock() != block) {
            return false;
        }

        if (tag != null && !state.is(tag)) {
            return false;
        }

        for (var entry : states.entrySet()) {
            Property<?> prop = entry.getKey();
            String expected = entry.getValue();

            if (!state.hasProperty(prop)) return false;
            if (!state.getValue(prop).toString().equals(expected)) return false;
        }

        return true;
    }

    public static BlockMatchRule parseRule(String input) {
        String[] parts = input.split("\\[", 2);
        String targetPart = parts[0];

        Block block = null;
        TagKey<Block> tag = null;

        if (targetPart.equals("%") || targetPart.equals("?") || targetPart.equals("*") || targetPart.equalsIgnoreCase("any")) {
            //null
        }
        else if (targetPart.startsWith("#")) {
            String tagId = targetPart.substring(1);
            if (!tagId.contains(":")) {
                tagId = "minecraft:" + tagId;
            }

            tag = TagKey.create(
                    Registries.BLOCK,
                    Objects.requireNonNull(ResourceLocation.tryParse(tagId))
            );
        }
        else {
            if (!targetPart.contains(":")) {
                targetPart = "minecraft:" + targetPart;
            }

            ResourceLocation id = ResourceLocation.tryParse(targetPart);
            //? if >=1.21.3 {
            /*block = BuiltInRegistries.BLOCK.getValue(id);
            *///?} else {
            block = BuiltInRegistries.BLOCK.get(id);
            //?}
        }

        Map<Property<?>, String> states = new HashMap<>();

        if (parts.length > 1) {
            String props = parts[1].replace("]", "");
            for (String prop : props.split(",")) {
                String[] kv = prop.split("=");
                if (kv.length != 2) continue;

                String key = kv[0];
                String value = kv[1];

                Property<?> property = resolveProperty(block, tag, key);
                if (property != null) {
                    states.put(property, value);
                }
            }
        }

        return new BlockMatchRule(block, tag, states);
    }

    @Nullable
    private static Property<?> resolveProperty(
            @Nullable Block block,
            @Nullable TagKey<Block> tag,
            String name
    ) {
        if (block != null) {
            return block.getStateDefinition().getProperty(name);
        }

        if (tag != null) {
            for (Block b : BuiltInRegistries.BLOCK) {
                if (b.defaultBlockState().is(tag)) {
                    Property<?> prop = b.getStateDefinition().getProperty(name);
                    if (prop != null) return prop;
                }
            }
            return null;
        }

        for (Block b : BuiltInRegistries.BLOCK) {
            Property<?> prop = b.getStateDefinition().getProperty(name);
            if (prop != null) return prop;
        }

        return null;
    }
}
