package net.marblednull.shotsfired.config;

import com.corrinedev.jsconf.api.Config;
import com.corrinedev.jsconf.api.ConfigValue;
import com.google.common.reflect.TypeToken;
import net.marblednull.shotsfired.util.DropData;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.marblednull.shotsfired.ShotsFired.MODID;

public class TACZConfig {
    public static final Config TACZ_CONFIG = new Config(MODID + "-tacz");

    public static final ConfigValue<Map<String, DropData>> TACZ
            = new ConfigValue<>(new LinkedHashMap<>(Map.of(
            "tacz:scar_l", new DropData("minecraft:apple", 50),
            "tacz:hk_mp5a5", new DropData("minecraft:gold_ingot", 50),
            "tacz:m700", new DropData("minecraft:copper_ingot", 50)

    )),
    "TACZ",
    TACZ_CONFIG,
    new TypeToken<Map<String, DropData>>(){}.getType()
    );

    public static void init() {
        TACZ_CONFIG.register();
    }
}