package net.mcjedev.mods.wikihelper.util;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import net.mcjedev.mods.wikihelper.WikiHelper;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;

import net.mcjedev.mods.wikihelper.WikiHelperExpectPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileHelper {
    private static final Logger LOGGER = LogManager.getLogger(FileHelper.class);

    public static void saveStable(JsonElement jsonElement, String fileName) {
        var dir = WikiHelperExpectPlatform.getConfigDirectory().resolve(WikiHelper.MOD_ID);
        var filePath = dir.resolve(fileName);

        try {
            Files.createDirectories(dir);
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
                 JsonWriter jsonWriter = new JsonWriter(writer)) {
                jsonWriter.setSerializeNulls(false);
                jsonWriter.setIndent("  ");
                GsonHelper.writeValue(jsonWriter, jsonElement, Comparator.naturalOrder());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save file to {}", filePath.toUri(), e);
        }
    }

}
