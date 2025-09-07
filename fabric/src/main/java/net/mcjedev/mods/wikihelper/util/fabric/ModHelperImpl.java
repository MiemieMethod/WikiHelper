package net.mcjedev.mods.wikihelper.util.fabric;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ModHelperImpl {
    public static Optional<String> getModName(String namespace) {
        return FabricLoader.getInstance().getModContainer(namespace)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getName)
                .filter(Predicate.not(Strings::isNullOrEmpty));
    }

    public static List<String> getModList() {
        return FabricLoader.getInstance().getAllMods().stream()
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getId)
                .filter(Predicate.not(Strings::isNullOrEmpty))
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    public static JsonObject getModMetaObject(String namespace) {
        JsonObject metaObj = new JsonObject();
        metaObj.addProperty("id", namespace);
        ModContainer mod = FabricLoader.getInstance().getModContainer(namespace).orElse(null);
        if (mod != null) {
            ModMetadata meta = mod.getMetadata();
            addJsonArray(metaObj, "aliases", meta.getProvides());
            metaObj.addProperty("version", meta.getVersion().toString());
            metaObj.addProperty("environment", switch (meta.getEnvironment()) {
                case CLIENT -> "client";
                case SERVER -> "server";
                case UNIVERSAL -> "universal";
            });
            if (!meta.getDependencies().isEmpty()) {
                JsonObject metaDeps = new JsonObject();
                meta.getDependencies().forEach(dep -> {
                    if (!Strings.isNullOrEmpty(dep.getModId())) {
                        String kind = dep.getKind().getKey();
                        if (metaDeps.has(kind)) {
                            metaDeps.getAsJsonArray(kind).add(dep.getModId());
                        } else {
                            JsonArray arr = new JsonArray();
                            arr.add(dep.getModId());
                            metaDeps.add(kind, arr);
                        }
                    }
                });
                metaObj.add("dependencies", metaDeps);
            }
            metaObj.addProperty("name", meta.getName());
            metaObj.addProperty("description", meta.getDescription());
            addPersonArray(metaObj, "authors", meta.getAuthors());
            addPersonArray(metaObj, "contributors", meta.getContributors());
            ContactInformation contact = meta.getContact();
            getContactObject(contact).ifPresent(contactObj -> metaObj.add("contact", contactObj));
            addJsonArray(metaObj, "licenses", meta.getLicense());
            mod.getContainingMod().ifPresent(parent -> metaObj.addProperty("parent", parent.getMetadata().getId()));
            if (!mod.getContainedMods().isEmpty()) {
                JsonArray metaContained = new JsonArray();
                mod.getContainedMods().forEach(child -> {
                    if (!Strings.isNullOrEmpty(child.getMetadata().getId())) {
                        metaContained.add(child.getMetadata().getId());
                    }
                });
                metaObj.add("children", metaContained);
            }
        }

        return metaObj;
    }


    private static void addJsonArray(JsonObject obj, String key, Collection<String> values) {
        if (values == null || values.isEmpty()) return;
        var arr = new JsonArray();
        values.stream().filter(s -> !Strings.isNullOrEmpty(s)).forEach(arr::add);
        if (!arr.isEmpty()) obj.add(key, arr);
    }

    private static void addPersonArray(JsonObject obj, String key, Collection<Person> people) {
        if (people == null || people.isEmpty()) return;
        var arr = new JsonArray();
        people.stream()
                .map(ModHelperImpl::getPersonObject)
                .flatMap(Optional::stream)
                .forEach(arr::add);
        if (!arr.isEmpty()) obj.add(key, arr);
    }

    public static Optional<JsonObject> getPersonObject(Person person) {
        if (!Strings.isNullOrEmpty(person.getName())) {
            JsonObject personObj = new JsonObject();
            personObj.addProperty("name", person.getName());
            ContactInformation contact = person.getContact();
            getContactObject(contact).ifPresent(contactObj -> personObj.add("contact", contactObj));
            return Optional.of(personObj);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<JsonObject> getContactObject(ContactInformation contact) {
        JsonObject contactObj = new JsonObject();
        contact.asMap().forEach((key, value) -> {
            if (!Strings.isNullOrEmpty(value)) {
                contactObj.addProperty(key, value);
            }
        });
        if (!contactObj.isEmpty()) {
            return Optional.of(contactObj);
        } else {
            return Optional.empty();
        }
    }

}
