package net.marblednull.shotsfired;

import com.corrinedev.jsconf.api.Config;
import com.corrinedev.jsconf.api.ConfigValue;
import com.google.common.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.marblednull.shotsfired.ShotsFired.MODID;

/// Because we couldn't get the Forge Config to work for the life of us
/// Tweaked by MC7 and ChatGPT for burst fire.
public class TACZBurstConfig {
    public static final Config TACZ_BURST_CONFIG = new Config(MODID + "-tacz-burst");

    public static final ConfigValue<Map<String, BurstData>> TACZ_BURST
            = new ConfigValue<>(new LinkedHashMap<>(Map.of(
            "tacz:scar_l", new BurstData(1, 0.1),
            "tacz:hk_mp5a5", new BurstData(1, 0.1)

            )), "TACZ Burst",
            TACZ_BURST_CONFIG,
            new TypeToken<Map<String, TACZEjectionConfig.EjectionInfo>>(){}.getType()
            );

    public static void init() {
        TACZ_BURST_CONFIG.register();
    }

}