package net.mcjedev.mods.wikihelper.util;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import net.mcjedev.mods.wikihelper.WikiHelper;
import net.minecraft.util.GsonHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

import net.mcjedev.mods.wikihelper.WikiHelperExpectPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileHelper {
    private static final Logger LOGGER = LogManager.getLogger(FileHelper.class);

    public static void saveStable(JsonElement jsonElement, String fileName) {
        java.nio.file.Path dir = WikiHelperExpectPlatform.getConfigDirectory().resolve(WikiHelper.MOD_ID);
        java.nio.file.Path filePath = dir.resolve(fileName);
        File file = filePath.toFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (JsonWriter jsonWriter = new JsonWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            jsonWriter.setSerializeNulls(false);
            jsonWriter.setIndent("  ");
            GsonHelper.writeValue(jsonWriter, jsonElement, Comparator.naturalOrder());
        } catch (IOException iOException) {
            LOGGER.error("Failed to save file to {}", filePath.toUri(), iOException);
        }
    }

}
