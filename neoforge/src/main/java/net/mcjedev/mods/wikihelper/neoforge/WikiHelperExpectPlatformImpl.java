package net.mcjedev.mods.wikihelper.neoforge;

import net.mcjedev.mods.wikihelper.WikiHelperExpectPlatform;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class WikiHelperExpectPlatformImpl {
    /**
     * This is our actual method to {@link WikiHelperExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
