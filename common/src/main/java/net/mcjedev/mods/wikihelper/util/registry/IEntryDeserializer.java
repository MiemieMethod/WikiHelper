package net.mcjedev.mods.wikihelper.util.registry;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface IEntryDeserializer {
    void deserialize(Object entryValue, JsonObject entryObj);
}
