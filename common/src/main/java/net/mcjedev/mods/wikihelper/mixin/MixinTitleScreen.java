package net.mcjedev.mods.wikihelper.mixin;

import net.minecraft.client.gui.screens.TitleScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
    @Unique
    private static final Logger wiki_Helper_1_21$LOGGER = LogManager.getLogger(MixinTitleScreen.class);

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {
        wiki_Helper_1_21$LOGGER.info("Hello from wikihelper common mixin!");
    }
}