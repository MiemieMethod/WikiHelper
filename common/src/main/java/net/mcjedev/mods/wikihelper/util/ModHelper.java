package net.mcjedev.mods.wikihelper.util;

import com.google.gson.JsonObject;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.resources.language.I18n;

import java.util.List;
import java.util.Optional;

public class ModHelper {
    @ExpectPlatform
    public static Optional<String> getModName(String namespace) {
        throw new AssertionError();
    }

    public static Optional<String> getModNameI18n(String namespace) {
        String modMenuKey = "modmenu.nameTranslation.%s".formatted(namespace);
        return I18n.exists(modMenuKey) ? Optional.of(I18n.get(modMenuKey)) : Optional.empty();
    }

    @ExpectPlatform
    public static List<String> getModList() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static JsonObject getModMetaObject(String namespace) {
        throw new AssertionError();
    }
}
