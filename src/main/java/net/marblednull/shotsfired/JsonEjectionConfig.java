package net.marblednull.shotsfired;

import com.corrinedev.jsconf.api.Config;
import com.corrinedev.jsconf.api.ConfigValue;
import java.util.LinkedHashMap;
import java.util.Map;

/// new config rebuilt by MC7 on Corrine's JSConf and previous snippets from Corrine
public class JsonEjectionConfig {
    public static final Config EJECTION_CONFIG =  new Config("shotsfired-ejection-config");

    public record EjectionInfo(double casingVelocity, boolean isRight, double rotationAngle, double verticalScalingFactor, double verticalOffset, double sideOffsetDistance) { }
    public static final ConfigValue<EjectionInfo> EJECTION_VALUES = new ConfigValue<>(new EjectionInfo(1.0, true, 85.0, 1.0, -0.25, 0.15), "ejectionInfo", EJECTION_CONFIG);
    public static final ConfigValue<LinkedHashMap<String, EjectionInfo>> EJECTION_MAP = new ConfigValue<>(new LinkedHashMap<>(Map.of(

    "tacz:glock17", new EjectionInfo(1.0, true, 85.0, 1.0, -0.25, 0.15),
    "tacz:hk_mp5a5", new EjectionInfo(1.0, true, 85.0, 1.0, -0.25, 0.15)

    )), "Ejected Casing Data by GunId.", EJECTION_CONFIG);

    public static void register() { EJECTION_CONFIG.register(); }

}