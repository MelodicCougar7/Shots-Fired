package net.marblednull.shotsfired.config;

import com.corrinedev.jsconf.api.Config;
import com.corrinedev.jsconf.api.ConfigValue;
import com.google.common.reflect.TypeToken;
import java.util.LinkedHashMap;
import java.util.Map;
import net.marblednull.shotsfired.util.DropData;

public class TACZReloadConfig {
    public static final Config TACZ_RELOAD_CONFIG = new Config("shotsfired-tacz-reload");
    public static final ConfigValue<Map<String, DropData>> TACZ_RELOAD;

    public TACZReloadConfig() {
    }

    public static void init() {
        TACZ_RELOAD_CONFIG.register();
    }

    static {
        TACZ_RELOAD = new ConfigValue(new LinkedHashMap(Map.of("tacz:db_long", new DropData("minecraft:apple", 50.0F))), "TACZ_Reload", TACZ_RELOAD_CONFIG, (new TypeToken<Map<String, DropData>>() {
        }).getType());
    }
}
