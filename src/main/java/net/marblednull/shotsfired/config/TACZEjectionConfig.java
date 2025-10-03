package net.marblednull.shotsfired.config;

import com.corrinedev.jsconf.api.Config;
import com.corrinedev.jsconf.api.ConfigValue;
import com.google.common.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.Map;

/// new config rebuilt by MC7 on Corrine's JSConf and previous snippets from Corrine
public class TACZEjectionConfig {
    public static final Config TACZ_EJECTION_CONFIG =  new Config("shotsfired-tacz-ejection-config");

    public record EjectionInfo(double velocity, boolean isRight, double rotationYawDeg, double rotationPitchDeg, double rotationRollDeg, double offsetX, double offsetY, double offsetZ){}

    public static final ConfigValue<Map<String, EjectionInfo>> EJECTION_MAP = new ConfigValue<>(new LinkedHashMap<>(Map.of(

    "tacz:glock17", new EjectionInfo(1.0, true, 85.0, 1.0, -0.25, 0.15, 0.15, 0.15),
    "tacz:hk_mp5a5", new EjectionInfo(0.4, true, 85.0, -45.0, -25.0, 0.55, -0.2, 0.11)

    )),
    "Ejected Casing Data by GunId.",
    TACZ_EJECTION_CONFIG,
    new TypeToken<Map<String, EjectionInfo>>(){}.getType()
    );

    public static void init() { TACZ_EJECTION_CONFIG.register(); }

}