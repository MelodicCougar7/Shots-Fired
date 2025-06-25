package net.marblednull.shotsfired;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/// Test class to handle the casing creation event under weaponShootEvent()

public class ModEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final RandomSource randomBulletChance = RandomSource.create();

    // from corrine, config parser
//    public static HashMap<String, DropData> parseConfig() {
//        if (TACZConfig.CONFIG_MAP.isEmpty()) {
//            try {
//                TACZConfig.CONFIG_MAP = TACZConfig.readConfig();
//            } catch (IOException e) {
//                LOGGER.error("IOException when parsing TACZ Config.");
//                throw new RuntimeException(e);
//            }
//        }
//        // TEMPORARY LOGGING STATEMENT
//        LOGGER.info("Success when parsing TACZ Config.");
//        return TACZConfig.CONFIG_MAP;
//    }

//    public static HashMap<String, BurstData> parseBurstConfig() {
//        if (TACZBurstConfig.CONFIG_MAP.isEmpty()) {
//            try {
//                TACZBurstConfig.CONFIG_MAP = TACZBurstConfig.readConfig();
//                LOGGER.error("IOException when parsing TACZ Burst map.");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        // TEMPORARY LOGGING STATEMENT
//        LOGGER.info("Success when parsing TACZ Burst map.");
//        return TACZBurstConfig.CONFIG_MAP;
//    }

    //help from and credit to Leducklet/Corrineduck and ChatGPT smh

    public static void weaponShootEvent(com.tacz.guns.api.event.common.GunShootEvent gunEvent) {
        // TEMPORARY LOGGING STATEMENT
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
                ItemStack casingStack = new ItemStack(casingItem);
                // The chance the item will drop from the gun
                float dropChance = gunItemMap.get(gunId).chance;

                LOGGER.warn("Retrieving casing item for GunId: {}. Item is {}", gunId, casingItem);

                int shotCount = 1; // default value. Is overridden by the burst config as necessary
                BurstData LocalBurstInfo = new BurstData(1, 0.15); // default value. Is overridden by the config as necessary.

                if (gunEvent.getShooter().getMainHandItem().getTag().getString("GunFireMode").equals("BURST")) {
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
                        LOGGER.warn("Casing broke! Ignoring further shot creation");
                        continue; // make the
                    }
                    //Create casing entity with velocity

                    Vec3 lookDirectionDEPRECATEDFORNOW = gunEvent.getShooter().getLookAngle();

                    //CONFIGURABLE VALUES


                    //=====================================

                    // random stuff from Quanz I haven't worked out yet

                    double shootingHeight = gunEvent.getShooter().getY() + gunEvent.getShooter().getEyeHeight() / 1.3;
                    // Offset the bullet spawning position, we don't want the bullet blocking player vision in first person
                    double offsetSize = 0.75f;

                    // ====================================
                    Map<String, TACZEjectionConfig.EjectionInfo> ejectionConfigMap = TACZEjectionConfig.EJECTION_MAP.get();
                    TACZEjectionConfig.EjectionInfo ejectionInfo = ejectionConfigMap.get(gunId);

                    // Get the player's look vector
                    Vec3 lookDirection = gunEvent.getShooter().getLookAngle();

                    // determine whether ejection is to the left or right
                    boolean isRight = ejectionInfo.isRight();
                    if (!isRight) { // if it does NOT eject on the right, continue
                        lookDirection = new Vec3(-lookDirection.z, lookDirection.y, lookDirection.x); // simple left side mirror. Requires testing
                    }

                    // Apply 3D rotation
                    lookDirection = rotateVector(lookDirection,
                            Math.toRadians(ejectionInfo.rotationYawDeg()),
                            Math.toRadians(ejectionInfo.rotationPitchDeg()),
                            Math.toRadians(ejectionInfo.rotationRollDeg())
                    );

                    // Normalize and scale velocity
                    Vec3 velocity = lookDirection.normalize().scale(ejectionInfo.velocity());

                    // Determine spawn offset relative to player’s look direction
                    Vec3 right = lookDirection.cross(new Vec3(0, 1, 0)).normalize(); // right direction
                    Vec3 up = new Vec3(0, 1, 0);                            // up direction
                    Vec3 forward = lookDirection.normalize();

                    Vec3 offset = right.scale(ejectionInfo.offsetX()) // verify that this actually works relative to the player
                            .add(up.scale(ejectionInfo.offsetY()))
                            .add(forward.scale(ejectionInfo.offsetZ()));

                    // Final spawn position, including adjustments
                    Vec3 spawnPos = new Vec3(
                            gunEvent.getShooter().getX() + offset.x,
                            gunEvent.getShooter().getY() + offset.y,
                            gunEvent.getShooter().getZ() + offset.z
                    );


                    // Spawn the item
                    ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), spawnPos.x, spawnPos.y, spawnPos.z, casingStack.copy());
                    casing.setDeltaMovement(velocity);
                    casing.setDefaultPickUpDelay();

                    gunEvent.getShooter().level().addFreshEntity(casing);

                    // wait a specified amount of time
                    try {
                        Thread.sleep((long) (LocalBurstInfo.delay * 1000)); // Convert seconds to ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } // end of gunItemMap,gunId check, and casing spawning
        }
    }

    private static Vec3 rotateVector(Vec3 vec, double yaw, double pitch, double roll) {
        // Yaw (rotation around Y-axis)
        double x1 = vec.x * Math.cos(yaw) - vec.z * Math.sin(yaw);
        double z1 = vec.x * Math.sin(yaw) + vec.z * Math.cos(yaw);
        double y1 = vec.y;

        // Pitch (rotation around X-axis)
        double y2 = y1 * Math.cos(pitch) - z1 * Math.sin(pitch);
        double z2 = y1 * Math.sin(pitch) + z1 * Math.cos(pitch);
        double x2 = x1;

        // Roll (rotation around Z-axis)
        double x3 = x2 * Math.cos(roll) - y2 * Math.sin(roll);
        double y3 = x2 * Math.sin(roll) + y2 * Math.cos(roll);
        double z3 = z2;

        return new Vec3(x3, y3, z3);
    }
}