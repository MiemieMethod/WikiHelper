package net.mcjedev.mods.wikihelper.quilt;

import net.mcjedev.mods.wikihelper.fabriclike.WikiHelperFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class WikiHelperQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        WikiHelperFabricLike.init();
    }
}
