package net.mcjedev.mods.wikihelper.fabric;

import net.mcjedev.mods.wikihelper.WikiHelperExpectPlatform;
import org.quiltmc.loader.api.QuiltLoader;

import java.nio.file.Path;

public class WikiHelperExpectPlatformImpl {
    /**
     * This is our actual method to {@link WikiHelperExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return QuiltLoader.getConfigDir();
    }
}
