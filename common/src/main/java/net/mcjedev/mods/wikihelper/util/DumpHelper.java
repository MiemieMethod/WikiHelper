package net.mcjedev.mods.wikihelper.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DumpHelper {
    private static final Logger LOGGER = LogManager.getLogger(DumpHelper.class);
    public static final Map<String, MappedRegistry<?>> TRACKED_REGISTRIES = new java.util.HashMap<>();
    static Map<String, Map<String, Map<String, Object>>> REGISTRIES_DATA = new java.util.HashMap<>();
    static Map<String, Map<String, Object>> REGISTRIES_REGISTRATION = new java.util.HashMap<>();
    static Set<String> KNOWN_MODS = new java.util.HashSet<>();

    public static void init(MinecraftServer server) {
        updateMods();
        registries2Data();
        dumpData();
    }

    @SuppressWarnings("unchecked")
    public static WritableRegistry<WritableRegistry<?>> accessBuiltInRegistries() {
        WritableRegistry<WritableRegistry<?>> writableRegistries = null;
        try {
            Field writableRegistriesField = BuiltInRegistries.class.getDeclaredField("WRITABLE_REGISTRY");
            writableRegistriesField.setAccessible(true);
            writableRegistries = (WritableRegistry<WritableRegistry<?>>) writableRegistriesField.get(null);
        } catch (Exception e) {
            LOGGER.error("Failed to access BuiltInRegistries.WRITABLE_REGISTRY", e);
        }
        return writableRegistries;
    }

    public static void registries2Data() {
        TRACKED_REGISTRIES.forEach((key, registry) -> {
            Stream<? extends Holder.Reference<?>> holderStream = registry.asLookup().listElements();
            holderStream.forEach(holder -> {
                ResourceLocation identifier = holder.key().location();
                String namespace = identifier.getNamespace();
                KNOWN_MODS.add(namespace);
                REGISTRIES_DATA.computeIfAbsent(namespace, k -> new java.util.HashMap<>());
                REGISTRIES_DATA.get(namespace).computeIfAbsent(key, k -> new java.util.HashMap<>());
                REGISTRIES_DATA.get(namespace).get(key).put(identifier.toString(), holder.value());
            });
            String namespace = ResourceLocation.parse(key).getNamespace();
            KNOWN_MODS.add(namespace);
            REGISTRIES_REGISTRATION.computeIfAbsent(namespace, k -> new java.util.HashMap<>());
            REGISTRIES_REGISTRATION.get(namespace).put(key, registry);
        });
    }

    public static void updateMods() {
        KNOWN_MODS.addAll(ModHelper.getModList());
    }

    public static void dumpData() {
        KNOWN_MODS.forEach((namespace) -> {
            Map<String, Map<String, Object>> registryMap = REGISTRIES_DATA.getOrDefault(namespace, Map.of());
            JsonObject dataObj = new JsonObject();
            dataObj.add("meta", ModHelper.getModMetaObject(namespace));
            JsonObject regObj = new JsonObject();
            Map<String, Object> registration = REGISTRIES_REGISTRATION.computeIfAbsent(namespace, k -> new java.util.HashMap<>());
            registration.forEach((registryName, registryValue) -> {
                JsonObject entryObj = new JsonObject();
                entryObj.addProperty("class", registryValue.getClass().getName());
                entryObj.addProperty("toString", registryValue.toString());
                regObj.add(registryName, entryObj);
            });
            dataObj.add("registration", regObj);
            JsonObject builtinObj = new JsonObject();
            registryMap.forEach((registryName, entries) -> {
                JsonObject registryObj = new JsonObject();
                entries.forEach((entryName, entryValue) -> {
                    JsonObject entryObj = new JsonObject();
                    entryObj.addProperty("class", entryValue.getClass().getName());
                    entryObj.addProperty("toString", entryValue.toString());
                    registryObj.add(entryName, entryObj);
                });
                builtinObj.add(registryName, registryObj);
            });
            dataObj.add("registries", builtinObj);
            FileHelper.saveStable(dataObj, "mods/" + namespace + ".json");
            LOGGER.info("Dumped data for namespace: {}", namespace);
        });
    }
}
