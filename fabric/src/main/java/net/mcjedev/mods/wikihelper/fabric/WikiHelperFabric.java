package net.mcjedev.mods.wikihelper.fabric;

import net.mcjedev.mods.wikihelper.fabriclike.WikiHelperFabricLike;
import net.fabricmc.api.ModInitializer;

public class WikiHelperFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WikiHelperFabricLike.init();
    }
}
