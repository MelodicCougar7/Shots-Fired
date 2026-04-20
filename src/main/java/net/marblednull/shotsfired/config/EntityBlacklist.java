package net.marblednull.shotsfired.config;

import com.corrinedev.jsconf.api.Config;
import com.corrinedev.jsconf.api.ConfigValue;
import com.google.common.reflect.TypeToken;
import net.marblednull.shotsfired.util.DropData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.marblednull.shotsfired.ShotsFired.MODID;

public class EntityBlacklist {

    public static final Config ENTITY_BLACKLIST = new Config(MODID + "-entity_blacklist");

    public static final ConfigValue<List<String>> BLACKLIST = new ConfigValue(List.of("minecraft:pig"), "EntityBlacklist", ENTITY_BLACKLIST, (new TypeToken<List<String>>() {
    }).getType());

    public static void init() {
        ENTITY_BLACKLIST.register();
    }
}
