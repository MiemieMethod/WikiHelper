package net.mcjedev.mods.wikihelper.util.registry;

import com.google.gson.JsonObject;
import net.minecraft.world.level.block.Block;

public class MinecraftBlockDeserializer implements IEntryDeserializer {
    @Override
    public void deserialize(Object entryValue, JsonObject entryObj) {
        var block = (Block) entryValue;

        entryObj.addProperty("description_id", block.getDescriptionId());
        entryObj.addProperty("item", block.asItem().toString());
    }
}