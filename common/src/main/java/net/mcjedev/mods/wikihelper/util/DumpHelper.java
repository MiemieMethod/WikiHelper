package net.mcjedev.mods.wikihelper.util;

import java.util.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.mcjedev.mods.wikihelper.util.registry.IEntryDeserializer;
import net.mcjedev.mods.wikihelper.util.registry.MinecraftItemEntryDeserializer;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DumpHelper {
    private static final Logger LOGGER = LogManager.getLogger(DumpHelper.class);

    public static boolean INITIALIZED = false;
    public static RegistryOps<JsonElement> REGISTRY_OPS;
    public static final Set<String> KNOWN_MODS = new java.util.HashSet<>();
    public static final Map<String, MappedRegistry<Object>> TRACKED_REGISTRIES = new java.util.HashMap<>();
    public static final Map<String, Map<String, MappedRegistry<Object>>> REGISTRIES_REGISTRATION = new java.util.HashMap<>();
    public static final Map<String, Map<String, Map<String, Object>>> REGISTRIES_DATA = new java.util.HashMap<>();
    public static final Map<String, IEntryDeserializer> ENTRY_DESERIALIZER_REGISTRY = new HashMap<>();

    public static void init(MinecraftServer server) {
        updateKnownMods();
        resortRegistriesByNamespace();
        registerRegistryDeserializers();
    }

    public static void dump(MinecraftServer server) {
        if (!INITIALIZED) {
            init(server);
            INITIALIZED = true;
        }
        updateRegistryOps(server);
        dumpMods();
        releaseRegistryOps();
    }

    @SuppressWarnings("unchecked")
    public static WritableRegistry<WritableRegistry<?>> accessBuiltInRegistries() {
        try {
            var field = BuiltInRegistries.class.getDeclaredField("WRITABLE_REGISTRY");
            field.setAccessible(true);
            return (WritableRegistry<WritableRegistry<?>>) field.get(null);
        } catch (Exception e) {
            LOGGER.error("Failed to access BuiltInRegistries.WRITABLE_REGISTRY", e);
            return null;
        }
    }

    public static void updateKnownMods() {
        KNOWN_MODS.addAll(ModHelper.getModList());
    }

    public static void resortRegistriesByNamespace() {
        TRACKED_REGISTRIES.forEach((key, registry) -> {
            registry.asLookup().listElements().forEach(holder -> {
                var resourceKey = holder.key();
                var identifier = resourceKey.location();
                var namespace = identifier.getNamespace();
                KNOWN_MODS.add(namespace);

                var nsData = REGISTRIES_DATA.computeIfAbsent(namespace, k -> new HashMap<>());
                var regData = nsData.computeIfAbsent(key, k -> new HashMap<>());
                regData.put(identifier.toString(), holder.value());
            });

            var namespace = ResourceLocation.parse(key).getNamespace();
            KNOWN_MODS.add(namespace);
            REGISTRIES_REGISTRATION
                    .computeIfAbsent(namespace, k -> new HashMap<>())
                    .put(key, registry);
        });
    }

    public static void registerRegistryDeserializers() {
        ENTRY_DESERIALIZER_REGISTRY.put("minecraft:item", new MinecraftItemEntryDeserializer());
    }

    public static void updateRegistryOps(MinecraftServer server) {
        REGISTRY_OPS = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
    }

    public static void releaseRegistryOps() {
        REGISTRY_OPS = null;
    }

    public static void dumpMods() {
        KNOWN_MODS.forEach(namespace -> {
            var dataObj = new JsonObject();
            dataObj.add("meta", ModHelper.getModMetaObject(namespace));

            var regObj = new JsonObject();
            REGISTRIES_REGISTRATION
                    .computeIfAbsent(namespace, k -> new HashMap<>())
                    .forEach((registryName, registryValue) -> {
                        var entryObj = new JsonObject();
//                        entryObj.addProperty("class", registryValue.getClass().getName());
//                        entryObj.addProperty("toString", registryValue.toString());
                        entryObj.addProperty("key", registryValue.key().location().toString());
                        regObj.add(registryName, entryObj);
                    });
            dataObj.add("registration", regObj);

            var builtinObj = new JsonObject();
            REGISTRIES_DATA.getOrDefault(namespace, Map.of())
                    .forEach((registryName, entries) -> {
                        var deserializer = ENTRY_DESERIALIZER_REGISTRY.get(registryName);
                        var registryObj = new JsonObject();
                        entries.forEach((entryName, entryValue) -> {
                            var entryObj = new JsonObject();
//                            entryObj.addProperty("class", entryValue.getClass().getName());
//                            entryObj.addProperty("toString", entryValue.toString());
                            if (deserializer != null) {
                                deserializer.deserialize(entryValue, entryObj);
                            }
                            registryObj.add(entryName, entryObj);
                        });
                        builtinObj.add(registryName, registryObj);
                    });
            dataObj.add("registries", builtinObj);

            FileHelper.saveStable(dataObj, "mods/" + namespace + ".json");
            LOGGER.info("Dumped mod data for namespace: {}", namespace);
        });
    }
}
