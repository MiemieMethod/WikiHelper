package net.mcjedev.mods.wikihelper.forge;

import dev.architectury.platform.forge.EventBuses;
import net.mcjedev.mods.wikihelper.WikiHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WikiHelper.MOD_ID)
public class WikiHelperForge {
    public WikiHelperForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(WikiHelper.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        WikiHelper.init();
    }
}
