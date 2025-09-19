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
        var metaObj = new JsonObject();

        FabricLoader.getInstance().getModContainer(namespace).ifPresentOrElse(mod -> {
            var meta = mod.getMetadata();
            metaObj.addProperty("id", meta.getId());
            addJsonArray(metaObj, "aliases", meta.getProvides());
            metaObj.addProperty("version", meta.getVersion().toString());
            metaObj.addProperty("environment", switch (meta.getEnvironment()) {
                case CLIENT -> "client";
                case SERVER -> "server";
                case UNIVERSAL -> "universal";
            });
            if (!meta.getDependencies().isEmpty()) {
                var metaDeps = new JsonObject();
                meta.getDependencies().stream()
                        .filter(dep -> !Strings.isNullOrEmpty(dep.getModId()))
                        .forEach(dep -> {
                            var kind = dep.getKind().getKey();
                            var arr = metaDeps.has(kind) ? metaDeps.getAsJsonArray(kind) : new JsonArray();
                            arr.add(dep.getModId());
                            metaDeps.add(kind, arr);
                        });
                metaObj.add("dependencies", metaDeps);
            }
            metaObj.addProperty("name", meta.getName());
            metaObj.addProperty("description", meta.getDescription());
            addPersonArray(metaObj, "authors", meta.getAuthors());
            addPersonArray(metaObj, "contributors", meta.getContributors());
            getContactObject(meta.getContact()).ifPresent(contactObj -> metaObj.add("contact", contactObj));
            addJsonArray(metaObj, "licenses", meta.getLicense());
            mod.getContainingMod().ifPresent(parent -> metaObj.addProperty("parent", parent.getMetadata().getId()));
            if (!mod.getContainedMods().isEmpty()) {
                var metaContained = new JsonArray();
                mod.getContainedMods().stream()
                        .map(child -> child.getMetadata().getId())
                        .filter(Predicate.not(Strings::isNullOrEmpty))
                        .forEach(metaContained::add);
                metaObj.add("children", metaContained);
            }
        }, () -> metaObj.addProperty("id", namespace));

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
        if (Strings.isNullOrEmpty(person.getName())) return Optional.empty();
        var personObj = new JsonObject();
        personObj.addProperty("name", person.getName());
        getContactObject(person.getContact()).ifPresent(contactObj -> personObj.add("contact", contactObj));
        return Optional.of(personObj);
    }

    public static Optional<JsonObject> getContactObject(ContactInformation contact) {
        var contactObj = new JsonObject();
        contact.asMap().forEach((key, value) -> {
            if (!Strings.isNullOrEmpty(value)) {
                contactObj.addProperty(key, value);
            }
        });
        return contactObj.isEmpty() ? Optional.empty() : Optional.of(contactObj);
    }

}
