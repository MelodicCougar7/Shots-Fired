package net.marblednull.shotsfired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/// Because we couldn't get the Forge Config to work for the life of us
/// Tweaked by MC7 and ChatGPT for ejected casing velocity.
public class JsonEjectionConfig {
    public static final Path DIR = FMLPaths.CONFIGDIR.get();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static HashMap<String, Object> CONFIG_MAP = new HashMap<>();

    public static HashMap<String, Object> readConfig() throws IOException {
        File file = DIR.resolve("shotsfiredejection.json").toFile();
        if (file.exists()) {
            FileReader reader = new FileReader(file);
            JsonArray jsonArray = GSON.fromJson(reader, JsonArray.class);
            HashMap<String, Object> map = new HashMap<>();
            // I have given up understanding in favor of ChatGPT'ing my way to a functional code. Will update to relfect Corrine's JasonConfig when the documentation for that is released
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject config = jsonArray.get(i).getAsJsonObject();
                String gunId = config.get("gunId").getAsString();

                // Extracting each configuration value with fallback defaults
                double casingVelocity = config.has("casingVelocity") ? config.get("casingVelocity").getAsDouble() : 0.2;
                int isRight = config.has("isRight") ? config.get("isRight").getAsInt() : 1;
                double rotationAngle = config.has("rotationAngle") ? config.get("rotationAngle").getAsDouble() : 85.0;
                double verticalScalingFactor = config.has("verticalScalingFactor") ? config.get("verticalScalingFactor").getAsDouble() : 1.0;
                double verticalOffset = config.has("verticalOffset") ? config.get("verticalOffset").getAsDouble() : -0.25;
                double sideOffsetDistance = config.has("sideOffsetDistance") ? config.get("sideOffsetDistance").getAsDouble() : 0.15;

                // Store the configuration for this gunId
                HashMap<String, Object> casingConfig = new HashMap<>();
                casingConfig.put("casingVelocity", casingVelocity);
                casingConfig.put("isRight", isRight);
                casingConfig.put("rotationAngle", rotationAngle);
                casingConfig.put("verticalScalingFactor", verticalScalingFactor);
                casingConfig.put("verticalOffset", verticalOffset);
                casingConfig.put("sideOffsetDistance", sideOffsetDistance);

                // Add the configuration to the map with the gunId as the key
                map.put(gunId, casingConfig);
            }

            return map;
        } else {
            checkConfig();
            return new HashMap<String, Object>();
        }
    }

    public static void checkConfig() throws IOException {
        File file = DIR.resolve("shotsfiredejection.json").toFile();
        if (!file.exists()) {
            FileWriter writer = new FileWriter(file);
            JsonArray strArr = new JsonArray();
            // Example configuration, which will be used if the file doesn't exist yet

            JsonObject hkMp5Config = new JsonObject();
            hkMp5Config.addProperty("gunId", "tacz:hk_mp5a5");
            hkMp5Config.addProperty("casingVelocity", 0.2);
            hkMp5Config.addProperty("isRight", 1);
            hkMp5Config.addProperty("rotationAngle", 85);
            hkMp5Config.addProperty("verticalScalingFactor", 1.0);
            hkMp5Config.addProperty("verticalOffset", -0.25);
            hkMp5Config.addProperty("sideOffsetDistance", 0.15);

            JsonObject scarLConfig = new JsonObject();
            scarLConfig.addProperty("gunId", "tacz:scar_l");
            scarLConfig.addProperty("casingVelocity", 0.25);
            scarLConfig.addProperty("isRight", 0);
            scarLConfig.addProperty("rotationAngle", 90);
            scarLConfig.addProperty("verticalScalingFactor", 1.2);
            scarLConfig.addProperty("verticalOffset", -0.3);
            scarLConfig.addProperty("sideOffsetDistance", 0.2);

            // Add more gun configurations as needed
            strArr.add(hkMp5Config);
            strArr.add(scarLConfig);

            System.out.println("JSON CONFIG GEN = " + GSON.toJson(strArr));
            writer.write(GSON.toJson(strArr));
            writer.close();
        }
        CONFIG_MAP = readConfig();
    }


}