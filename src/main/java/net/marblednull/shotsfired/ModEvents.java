package net.marblednull.shotsfired;

import com.mojang.logging.LogUtils;
import net.marblednull.shotsfired.config.TACZBurstConfig;
import net.marblednull.shotsfired.config.TACZConfig;
import net.marblednull.shotsfired.config.TACZEjectionConfig;
import net.marblednull.shotsfired.util.BurstData;
import net.marblednull.shotsfired.util.DropData;
import net.marblednull.shotsfired.util.LaunchItemUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Map;

import static net.marblednull.shotsfired.util.LaunchItemUtil.spawnAndLaunchItem;

// Test class to handle the casing creation event under weaponShootEvent()

public class ModEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final RandomSource randomBulletChance = RandomSource.create();

    //help from and credit to Leducklet/Corrineduck and ChatGPT smh

    public static void weaponShootEvent(com.tacz.guns.api.event.common.GunShootEvent gunEvent) {
        // TEMPORARY LOGGING STATEMENTS COMMENTED OUT BUT LEFT FOR WHEN/IF I REFACTOR
        LOGGER.warn("weaponShootEvent called");
        if (gunEvent.getLogicalSide().isServer()) {
            Map<String, DropData> gunItemMap = TACZConfig.TACZ.get();

            // Get the GunId from the event
            String gunId = gunEvent.getGunItemStack().getTag().getString("GunId");

            // Check if the GunId exists in the map
            if (gunItemMap.containsKey(gunId)) {
                // Get the item associated with the GunId
                Item casingItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(gunItemMap.get(gunId).item));
                // create new itemstack from the retrieved GunId
                //ItemStack casingStack = new ItemStack(casingItem); // we'll use casingItem later
                // The chance the item will drop from the gun
                float dropChance = gunItemMap.get(gunId).chance;

                LOGGER.warn("Retrieving casing item for GunId: {}. Item is {}", gunId, casingItem);

                int shotCount = 1; // default value. Is overridden by the burst config as necessary
                BurstData LocalBurstInfo = new BurstData(1, 0.15); // default value. Is overridden by the config as necessary.

                boolean isBurst = gunEvent.getShooter().getMainHandItem().getTag().getString("GunFireMode").equals("BURST");
                if (isBurst) {
                    Map<String, BurstData> gunBurstMap = TACZBurstConfig.TACZ_BURST.get();
                    if (gunBurstMap.containsKey(gunId)) {
                        LocalBurstInfo = gunBurstMap.get(gunId); // redundant but protects against incomplete configs
                        shotCount = LocalBurstInfo.shotCount;
                    }
                    //burst fire mode spawning two casings, main difference between this and below code and will eventually swap for handler method once I learn how to properly create one

                    LOGGER.warn("Shot count is {}", shotCount);

                }
                // loop such that 1 passed shot count = one casing spawn attempt. Built for compatibility with burst shots under tacz's system.
                for (int i = 0; i < shotCount; i++) {
                    LOGGER.warn("Attempting shot.");

                    //Create casing entity

                    // Allow casing creation if this is true. This is a for loop, it will trigger on each passed shot, NOT groups, like burst.
                    // dropChance is thus the chance to NOT continue.
                    if (dropChance < randomBulletChance.nextFloat() * 100) {
                        //LOGGER.warn("Casing broke! Ignoring further shot creation");
                        continue; // make the casing
                    }
                    //Create casing entity with velocity

                    //=====================================

                    // random stuff from Quanz I haven't worked out yet. Will incorporate only if the ejection config system isn't enough

                    double shootingHeight = gunEvent.getShooter().getY() + gunEvent.getShooter().getEyeHeight() / 1.3;
                    // Offset the bullet spawning position, we don't want the bullet blocking player vision in first person
                    double offsetSize = 0.75f;

                    // ====================================

                    Map<String, TACZEjectionConfig.EjectionInfo> ejectionConfigMap = TACZEjectionConfig.EJECTION_MAP.get();
                    TACZEjectionConfig.EjectionInfo ejectionInfoByGun = ejectionConfigMap.get(gunId);

                    // stuff for the spawn and launch method
                    Player player = (Player) gunEvent.getShooter();
                    double forwardOffset = ejectionInfoByGun.offsetX();
                    double sideOffset = ejectionInfoByGun.offsetY();
                    double upOffset = ejectionInfoByGun.offsetZ();
                    double yawOffset = ejectionInfoByGun.rotationYawDeg();
                    double pitchOffset = ejectionInfoByGun.rotationPitchDeg();
                    double rollOffset = ejectionInfoByGun.rotationRollDeg();
                    double velocity = ejectionInfoByGun.velocity();

                    spawnAndLaunchItem(player, forwardOffset, sideOffset, upOffset, yawOffset, pitchOffset, rollOffset, velocity, casingItem);

                    // wait a specified amount of time
                    if (isBurst) {
                        try {
                            Thread.sleep((long) (LocalBurstInfo.delay * 1000)); // Convert seconds to ms
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } // end of gunItemMap,gunId check, and casing spawning
        }
    }
}