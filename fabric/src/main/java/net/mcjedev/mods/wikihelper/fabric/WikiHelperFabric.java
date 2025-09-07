package net.mcjedev.mods.wikihelper.fabric;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.mcjedev.mods.wikihelper.fabriclike.WikiHelperFabricLike;
import net.fabricmc.api.ModInitializer;

import net.mcjedev.mods.wikihelper.util.CommandHelper;

public class WikiHelperFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WikiHelperFabricLike.init();
    }
}
