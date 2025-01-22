package net.marblednull.shotsfired;
//credit to Leducklet/Corrineduck for code inspiration here.

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec.ConfigValue<List<String>> CASING_TO_GUNID = BUILDER.define("gunCasingMatch", List.of("tacz:glock_17|minecraft:apple", "tacz:cz75|minecraft:apple"));

    public static final ForgeConfigSpec SPEC = BUILDER.build();

}