package net.mcjedev.mods.wikihelper.neoforge;

import net.mcjedev.mods.wikihelper.WikiHelper;
import net.neoforged.fml.common.Mod;

@Mod(WikiHelper.MOD_ID)
public class WikiHelperNeoForge {
    public WikiHelperNeoForge() {
        // Run our common setup.
        WikiHelper.init();
    }
}
