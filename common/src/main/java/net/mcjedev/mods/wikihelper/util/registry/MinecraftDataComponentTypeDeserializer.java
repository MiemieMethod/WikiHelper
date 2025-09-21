package net.mcjedev.mods.wikihelper.util.registry;

import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponentType;

public class MinecraftDataComponentTypeDeserializer  implements IEntryDeserializer {
    @Override
    public void deserialize(Object entryValue, JsonObject entryObj) {
        var dct = (DataComponentType<?>) entryValue;

        entryObj.addProperty("is_transient", dct.isTransient());
        // todo: schema
    }
}