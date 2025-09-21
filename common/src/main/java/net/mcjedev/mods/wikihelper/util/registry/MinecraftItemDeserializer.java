package net.mcjedev.mods.wikihelper.util.registry;

import java.util.*;

import com.google.gson.JsonObject;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.mcjedev.mods.wikihelper.util.DumpHelper.REGISTRY_OPS;

public class MinecraftItemDeserializer implements IEntryDeserializer {
    private static final Logger LOGGER = LogManager.getLogger(MinecraftItemDeserializer.class);

    @Override
    public void deserialize(Object entryValue, JsonObject entryObj) {
        var item = (Item) entryValue;

        entryObj.addProperty("description_id", item.getDescriptionId());
        if (item.hasCraftingRemainingItem()) {
            entryObj.addProperty("crafting_remaining_item", Objects.requireNonNull(item.getCraftingRemainingItem()).toString());
        }
        entryObj.addProperty("enchantment_value", item.getEnchantmentValue());

        var compObj = new JsonObject();
        item.components().forEach(component -> {
            var c = (TypedDataComponent<Object>) component;
            if (!c.type().isTransient()) {
                try {
                    var result = Objects.requireNonNull(c.type().codec()).encodeStart(REGISTRY_OPS, c.value());

                    result.result().ifPresentOrElse(json -> {
                        compObj.add(component.type().toString(), json);
                    }, () -> {
                        compObj.addProperty(component.type().toString(), component.value().toString());
                        LOGGER.warn("Failed to deserialize component {} of item {}, reason: {}", component.type(), item.toString(), result.error());
                    });
                } catch (Exception e) {
                    compObj.addProperty(component.type().toString(), component.value().toString());
                    LOGGER.warn("Failed to validate component {} of item {}, reason: {}", component.type(), item.toString(), e.getMessage());
                }
            }
        });
        entryObj.add("components", compObj);
    }
}

