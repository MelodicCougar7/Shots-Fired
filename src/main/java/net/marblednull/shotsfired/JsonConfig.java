package net.marblednull.shotsfired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/// Because we couldn't get the Forge Config to work for the life of us

public class JsonConfig {
    public static final Path DIR = FMLPaths.CONFIGDIR.get();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static HashMap<String, Item> CONFIG_MAP = new HashMap<>();

    public static HashMap<String, Item> readConfig() throws IOException {
        File file = DIR.resolve("shotsfired.json").toFile();
        if(file.exists()) {
            FileReader reader = new FileReader(file);
            List<String> stringList = GSON.fromJson(reader, List.class);
            HashMap<String, Item> map = new HashMap<>();

            for (String strToParse : stringList) {
                String gunId;
                String itemId;
                String[] strs = strToParse.split("\\|");
                gunId = strs[0];
                itemId = strs[1];
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                if(item == Items.AIR) return new HashMap<>();
                map.put(gunId, item);
            }

            return map;
        } else {
            checkConfig();
            return new HashMap<String, Item>();
        }
    }

    public static void checkConfig() throws IOException {
        File file = DIR.resolve("shotsfired.json").toFile();
        if(!file.exists()) {
            FileWriter writer = new FileWriter(file);
            JsonArray strArr = new JsonArray();
            strArr.add("tacz:glock_17|minecraft:apple");
            strArr.add("tacz:cz75|minecraft:apple");
            System.out.println("JSON CONFIG GEN = " + GSON.toJson(strArr));
            writer.write(GSON.toJson(strArr));
            writer.close();
        }
        CONFIG_MAP = readConfig();
    }
}
