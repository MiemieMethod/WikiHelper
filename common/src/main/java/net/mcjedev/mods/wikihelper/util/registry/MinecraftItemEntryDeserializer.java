package net.mcjedev.mods.wikihelper.util.registry;

import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinecraftItemEntryDeserializer implements IEntryDeserializer {
    private static final Logger LOGGER = LogManager.getLogger(MinecraftItemEntryDeserializer.class);

    @Override
    public void deserialize(Object entryValue, JsonObject entryObj) {
        var item = (Item) entryValue;
        entryObj.addProperty("description_id", item.getDescriptionId());
        if (item.hasCraftingRemainingItem()) {
            entryObj.addProperty("crafting_remaining_item", Objects.requireNonNull(item.getCraftingRemainingItem()).toString());
        } else {
            entryObj.add("crafting_remaining_item", null);
        }
        entryObj.addProperty("enchantment_value", item.getEnchantmentValue());
        var compObj = new JsonObject();
        item.components().forEach(component -> {
            var cp = (TypedDataComponent<Object>) (Object) component;
            var result = Objects.requireNonNull(cp.type().codec()).encodeStart(JsonOps.INSTANCE, cp.value());
            if (result.result().isPresent()) {
                JsonElement json = result.result().get();
                compObj.add(component.type().toString(), json);
            } else {
                compObj.addProperty(component.type().toString(), component.value().toString());
                LOGGER.warn("Failed to serialize component {} of item {}, reason: {}", component.type(), item.toString(), result.error());
            }
        });
        entryObj.add("components", compObj);
    }
}

