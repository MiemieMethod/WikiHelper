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
        JsonObject metaObj = new JsonObject();
        metaObj.addProperty("id", namespace);
        ModContainer mod = ModList.get().getModContainerById(namespace).orElse(null);
        if (mod != null) {
            IModInfo modInfo = mod.getModInfo();
            metaObj.addProperty("name", modInfo.getDisplayName());
            metaObj.addProperty("description", modInfo.getDescription());
            metaObj.addProperty("version", modInfo.getVersion().toString());
            if (!modInfo.getDependencies().isEmpty()) {
                JsonObject metaDeps = new JsonObject();
                modInfo.getDependencies().forEach(dep -> {
                    if (!Strings.isNullOrEmpty(dep.getModId())) {
                        String kind = switch (dep.getType()) {
                            case REQUIRED -> "required";
                            case OPTIONAL -> "optional";
                            case INCOMPATIBLE -> "incompatible";
                            case DISCOURAGED -> "disallowed";
                        };
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
            metaObj.addProperty("namespace", modInfo.getNamespace());
            modInfo.getUpdateURL().ifPresent(url -> metaObj.addProperty("url_update", url.toExternalForm()));
            modInfo.getModURL().ifPresent(url -> metaObj.addProperty("url_mod", url.toExternalForm()));
        }

        return metaObj;
    }
}
