package net.marblednull.shotsfired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/// Because we couldn't get the Forge Config to work for the life of us
/// Tweaked by MC7 and ChatGPT for burst fire.
public class TACZBurstConfig {
    public static final Path DIR = FMLPaths.CONFIGDIR.get();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static HashMap<String, BurstData> CONFIG_MAP = new HashMap<>();

    public static HashMap<String, BurstData> readConfig() throws IOException {
        File file = DIR.resolve("shotsfired-tacz-burst.json").toFile();
        if(file.exists()) {
            FileReader reader = new FileReader(file);
            List<String> stringList = GSON.fromJson(reader, List.class);
            HashMap<String, BurstData> map = new HashMap<>();

            for (String strToParse : stringList) {
                String gunId;
                Integer shotCount;
                double delay = 0.1;
                String[] strs = strToParse.split("\\|");
                gunId = strs[0];
                try {
                    shotCount = Integer.parseInt(strs[1]); // Parse the integer value
                } catch (NumberFormatException e) {
                    shotCount = 0; // Default value in case of parsing error
                }
                map.put(gunId, new BurstData(shotCount, delay));
            }

            return map;
        } else {
            checkConfig();
            return new HashMap<String, BurstData>();
        }
    }



    public static void checkConfig() throws IOException {
        File file = DIR.resolve("shotsfired-tacz-burst.json").toFile();
        if(!file.exists()) {
            FileWriter writer = new FileWriter(file);
            JsonArray strArr = new JsonArray();
            strArr.add("tacz:hk_mp5a5|3");
            strArr.add("tacz:scar_l|3");
            System.out.println("JSON CONFIG GEN = " + GSON.toJson(strArr));
            writer.write(GSON.toJson(strArr));
            writer.close();
        }
        CONFIG_MAP = readConfig();
    }
}
