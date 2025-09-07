package net.mcjedev.mods.wikihelper.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import net.mcjedev.mods.wikihelper.WikiHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class CommandHelper {
    private static final Logger LOGGER = LogManager.getLogger(CommandHelper.class);
    public static void registerServerCommand(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registryAccess,
            Commands.CommandSelection environment) {
        registerDump(dispatcher);
    }

    public static void registerDump(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(WikiHelper.MOD_ID).then(Commands.literal("dump").executes(context -> {
            DumpHelper.init(context.getSource().getServer());
            context.getSource().sendSuccess(() -> Component.translatable("commands.wikihelper.success"), false);
            return 1;
        })));
    }
}
