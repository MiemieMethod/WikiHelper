package net.mcjedev.mods.wikihelper.fabric;

import net.mcjedev.mods.wikihelper.WikiHelperExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class WikiHelperExpectPlatformImpl {
    /**
     * This is our actual method to {@link WikiHelperExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
