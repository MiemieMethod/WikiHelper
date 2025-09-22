package net.mcjedev.mods.wikihelper.util.neoforge;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ModHelperImpl {
    public static Optional<String> getModName(String namespace) {
        return ModList.get().getModContainerById(namespace)
                .map(ModContainer::getModInfo)
                .map(IModInfo::getDisplayName)
                .filter(Predicate.not(Strings::isNullOrEmpty));
    }

    public static List<String> getModList() {
        return ModList.get().getMods().stream()
                .map(IModInfo::getModId)
                .filter(Predicate.not(Strings::isNullOrEmpty))
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    public static JsonObject getModMetaObject(String namespace) {
        var metaObj = new JsonObject();

        ModList.get().getModContainerById(namespace).ifPresentOrElse(mod -> {
            var modInfo = mod.getModInfo();
            metaObj.addProperty("id", modInfo.getModId());
            metaObj.addProperty("name", modInfo.getDisplayName());
            metaObj.addProperty("description", modInfo.getDescription());
            metaObj.addProperty("version", modInfo.getVersion().toString());
            if (!modInfo.getDependencies().isEmpty()) {
                var metaDeps = new JsonObject();
                modInfo.getDependencies().stream()
                        .filter(dep -> !Strings.isNullOrEmpty(dep.getModId()))
                        .forEach(dep -> {
                            var kind = dep.getType().name().toLowerCase();
                            var arr = metaDeps.has(kind) ? metaDeps.getAsJsonArray(kind) : new JsonArray();
                            arr.add(dep.getModId());
                            metaDeps.add(kind, arr);
                        });
                metaObj.add("dependencies", metaDeps);
            }
            metaObj.addProperty("namespace", modInfo.getNamespace());
            modInfo.getUpdateURL().ifPresent(url -> metaObj.addProperty("url_update", url.toExternalForm()));
            modInfo.getModURL().ifPresent(url -> metaObj.addProperty("url_mod", url.toExternalForm()));
        }, () -> metaObj.addProperty("id", namespace));

        return metaObj;
    }
}
