package net.mcjedev.mods.wikihelper;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.mcjedev.mods.wikihelper.util.CommandHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WikiHelper {
    private static final Logger LOGGER = LogManager.getLogger(WikiHelper.class);
    public static final String MOD_ID = "wikihelper";
    
    public static void init() {
        LOGGER.info(WikiHelperExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());

        CommandRegistrationEvent.EVENT.register(CommandHelper::registerServerCommand);
    }
}
