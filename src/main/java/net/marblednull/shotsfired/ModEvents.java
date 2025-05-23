package net.marblednull.shotsfired;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/// Main class to handle the casing creation event under weaponShootEvent()

public class ModEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final RandomSource randomBulletChance = RandomSource.create();

    // from corrine, config parser
    public static HashMap<String, DropData> parseConfig() {
        if (TACZConfig.CONFIG_MAP.isEmpty()) {
            try {
                TACZConfig.CONFIG_MAP = TACZConfig.readConfig();
            } catch (IOException e) {
                LOGGER.error("IOException when parsing TACZ Config.");
                throw new RuntimeException(e);
            }
        }
        // TEMPORARY LOGGING STATEMENT
        LOGGER.info("Success when parsing TACZ Config.");
        return TACZConfig.CONFIG_MAP;
    }

    public static HashMap<String, BurstData> parseBurstConfig() {
        if (TACZBurstConfig.CONFIG_MAP.isEmpty()) {
            try {
                TACZBurstConfig.CONFIG_MAP = TACZBurstConfig.readConfig();
                LOGGER.error("IOException when parsing TACZ Burst map.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // TEMPORARY LOGGING STATEMENT
        LOGGER.info("Success when parsing TACZ Burst map.");
        return TACZBurstConfig.CONFIG_MAP;
    }


    //help from and credit to Leducklet/Corrineduck and ChatGPT smh

    public static void weaponShootEvent(com.tacz.guns.api.event.common.GunShootEvent gunEvent) {
        // TEMPORARY LOGGING STATEMENT
        LOGGER.warn("weaponShootEvent called");
        if (gunEvent.getLogicalSide().isServer()) {
            HashMap<String, DropData> gunItemMap = parseConfig();

            // Get the GunId from the event
            String gunId = gunEvent.getGunItemStack().getTag().getString("GunId");

            // Check if the GunId exists in the map
            if (gunItemMap.containsKey(gunId)) {
                // Get the item associated with the GunId
                Item casingItem = gunItemMap.get(gunId).item;
                // create new itemstack from the retrieved GunId
                ItemStack casingStack = new ItemStack(casingItem);
                // The chance the item will drop from the gun
                float dropChance = gunItemMap.get(gunId).chance;

                LOGGER.warn("Retrieving casing item for GunId: {}. Item is {}", gunId, casingItem);

                int shotCount = 1; // default value. Is overridden by the burst config as necessary
                BurstData LocalBurstInfo = new BurstData(1, 0.15); // default value. Is overridden by the config as necessary.

                if (gunEvent.getShooter().getMainHandItem().getTag().getString("GunFireMode").equals("BURST")) {
                    HashMap<String, BurstData> gunBurstMap = parseBurstConfig();
                    if (gunBurstMap.containsKey(gunId)) {
                        LocalBurstInfo = gunBurstMap.get(gunId); // redundant but protects against incomplete configs
                        shotCount = LocalBurstInfo.shotCount;
                    }
                        //burst fire mode spawning two casings, main difference between this and below code and will eventually swap for handler method once I learn how to properly create one

                    LOGGER.warn("Shot count is {}",shotCount);

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

                    Vec3 lookDirection = gunEvent.getShooter().getLookAngle();

                    //CONFIGURABLE VALUES

                    LinkedHashMap<String, TACZEjectionConfig.EjectionInfo> ejectionConfigMap = TACZEjectionConfig.EJECTION_MAP.get();

                    // Reformat ejectionInfo to fix some wierd issue with jsconf, I believe
                    Object obj = ejectionConfigMap.get(gunId);
                    TACZEjectionConfig.EjectionInfo ejectionInfo;

                    if (obj instanceof TACZEjectionConfig.EjectionInfo ei) {
                        ejectionInfo = ei;
                    } else if (obj instanceof Map<?, ?> mapObj) {
                        // Manually re-parse it using Gson to get proper EjectionInfo
                        Gson gson = new Gson();
                        ejectionInfo = gson.fromJson(gson.toJson(mapObj), TACZEjectionConfig.EjectionInfo.class);
                        LOGGER.warn("Converted LinkedTreeMap to EjectionInfo for gunId '{}'", gunId);
                    } else {
                        // Fallback default
                        LOGGER.warn("Invalid or missing ejection data for '{}', using default.", gunId);
                        ejectionInfo = new TACZEjectionConfig.EjectionInfo(0.3, true, 90.0, 0.3, 0.1, 0.1);
                    }
                    //=====================================

                    // random stuff from Quanz I haven't worked out yet

                    double shootingHeight = gunEvent.getShooter().getY() + gunEvent.getShooter().getEyeHeight() / 1.3;
                    // Offset the bullet spawning position, we don't want the bullet blocking player vision in first person
                    double offsetSize = 0.75f;

                    // ====================================

                    //define casing velocity/speed
                    double velocity = ejectionInfo.casingVelocity();

                    //define whether ejection is on right or not. Default value is true
                    boolean isRight = ejectionInfo.isRight();

                    //define side to side eject
                    double rotationAngle = ejectionInfo.rotationAngle();

                    // define arc of casing eject
                    double verticalScalingFactor = ejectionInfo.verticalScalingFactor();

                    //adjust y position of casing spawn, from player Eye Height
                    double verticalOffset = ejectionInfo.verticalOffset();

                    //define side offset to the left or right of the player
                    double sideOffsetDistance = ejectionInfo.sideOffsetDistance();

                    //NOT A CONFIG OPTION, USED IN CALCULATIONS
                    double pitchAngle = gunEvent.getShooter().getXRot();

                    // CALCULATIONS, NOT VARIABLES, NO NEED FOR CHANGE

                    //DO NOT CHANGE - modify casing y position based on  player's eye height
                    double offsetY = gunEvent.getShooter().getEyeHeight();
                    //DO NOT CHANGE - add values of above 2
                    double adjustedY = offsetY + verticalOffset;
                    //DO NOT CHANGE - calculate spawn position based on isRight and sideOffsetDistance
                    Vector3d sideOffset = calculateSideOffset(lookDirection, isRight, sideOffsetDistance);

                    //Rotate 90 degrees from the look direction
                    Vector3d offsetDirection = rotateDirection(lookDirection, rotationAngle, isRight, pitchAngle, verticalScalingFactor);

                    //original casing entity creation below
                    //ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY(), gunEvent.getShooter().getZ(), casingStack.copy());

                    ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY() + adjustedY, gunEvent.getShooter().getZ(), casingStack.copy());

                    casing.setDeltaMovement(offsetDirection.x * velocity, offsetDirection.y * velocity, offsetDirection.z * velocity);

                    casing.setPickUpDelay(20);
                    //Add casing
                    gunEvent.getShooter().level().addFreshEntity(casing);

                    // wait a specified amount of time
                    try {
                        Thread.sleep((long)(LocalBurstInfo.delay * 1000)); // Convert seconds to ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                } // end of gunItemMap and gunId check
            }
        }


    public static Vector3d rotateDirection(Vec3 direction, double angleDegrees, boolean isLeft, double pitchAngle, double verticalScalingFactor) {
        // Convert angle to radians (since Java trigonometric functions use radians)
        double angleRadians = Math.toRadians(angleDegrees);
        double pitchRadians = Math.toRadians(pitchAngle); // Convert pitch to radians

        // Rotate around the Y-axis (horizontal rotation) by the given angle
        double cosAngle = Math.cos(angleRadians);
        double sinAngle = Math.sin(angleRadians);

        double offsetX, offsetZ;

        // Calculate horizontal rotation (around Y-axis)
        if (isLeft) {
            // Rotate counterclockwise (left)
            offsetX = direction.x() * cosAngle - direction.z() * sinAngle;
            offsetZ = direction.x() * sinAngle + direction.z() * cosAngle;
        } else {
            // Rotate clockwise (right)
            offsetX = direction.x() * cosAngle + direction.z() * sinAngle;
            offsetZ = -direction.x() * sinAngle + direction.z() * cosAngle;
        }

        // Adjust vertical direction (pitch) by incorporating the player's pitch and applying the vertical scaling factor
        double offsetY = Math.sin(pitchRadians) * verticalScalingFactor;  // Adjust Y (up/down) direction based on pitch angle and scaling factor

        // Return the rotated direction with the adjusted Y component
        return new Vector3d(offsetX, offsetY, offsetZ);
    }
    private static Vector3d calculateSideOffset(Vec3 direction, boolean isLeft, double distance) {
        // Calculate a perpendicular vector to the look direction
        // Rotate the vector 90 degrees around the Y-axis
        double offsetX, offsetZ;

        if (isLeft) {
            // Rotate counterclockwise (left)
            offsetX = -direction.z();  // Inverse of Z to get left direction
            offsetZ = direction.x();   // X is used as the Z component for left
        } else {
            // Rotate clockwise (right)
            offsetX = direction.z();   // Z is used as X component for right
            offsetZ = -direction.x();  // Inverse of X to get right direction
        }

        // Scale the perpendicular direction by the distance you want to offset
        offsetX *= distance;
        offsetZ *= distance;

        return new Vector3d(offsetX, 0, offsetZ);  // No change in Y, just horizontal movement
    }
}