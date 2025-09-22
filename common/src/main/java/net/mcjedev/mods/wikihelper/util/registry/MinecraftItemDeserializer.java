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
        item.components().forEach(c -> {
            handleComponent(item, c, compObj);
        });
        entryObj.add("components", compObj);
    }

    private static <T> void handleComponent(Item item, TypedDataComponent<T> component, JsonObject inOut) {
        if (!component.type().isTransient()) {
            try {
                var result = Objects.requireNonNull(component.type().codec()).encodeStart(REGISTRY_OPS, component.value());

                result.result().ifPresentOrElse(json -> {
                    inOut.add(component.type().toString(), json);
                }, () -> {
                    inOut.addProperty(component.type().toString(), component.value().toString());
                    LOGGER.warn("Failed to deserialize component {} of item {}, reason: {}", component.type(), item.toString(), result.error());
                });
            } catch (Exception e) {
                inOut.addProperty(component.type().toString(), component.value().toString());
                LOGGER.warn("Failed to validate component {} of item {}, reason: {}", component.type(), item.toString(), e.getMessage());
            }
        }
    }
}

