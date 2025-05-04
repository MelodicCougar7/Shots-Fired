package net.marblednull.shotsfired;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
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
import java.util.Objects;

public class ModEvents {
    // logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // from corrine, config parser
    public static HashMap<String, Item> parseConfig() {
        if (TACZConfig.CONFIG_MAP.isEmpty()) {
            try {
                TACZConfig.CONFIG_MAP = TACZConfig.readConfig();
            } catch (IOException e) {
                LOGGER.info("IOException when parsing TACZ Config.");
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Success when parsing TACZ Config.");
        return TACZConfig.CONFIG_MAP;
    }

    public static HashMap<String, Integer> parseBurstConfig() {
        if (TACZBurstConfig.CONFIG_MAP.isEmpty()) {
            try {
                TACZBurstConfig.CONFIG_MAP = TACZBurstConfig.readConfig();
            } catch (IOException e) {
                LOGGER.info("IOException when parsing TACZ Burst map.");
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Success when parsing TACZ Burst map.");
        return TACZBurstConfig.CONFIG_MAP;
    }

    //help from and credit to Leducklet/Corrineduck and ChatGPT smh

    public static void weaponShootEvent(com.tacz.guns.api.event.common.GunShootEvent gunEvent) {

        if (gunEvent.getLogicalSide().isServer()) {
            LOGGER.info("If 1.");
            HashMap<String, Item> gunItemMap = parseConfig(); // check here next I suppose

            // Get the GunId from the event
            String gunId = Objects.requireNonNull(gunEvent.getGunItemStack().getTag()).getString("GunId");
            LOGGER.info("GunId: {}", gunId);

            // Check if the GunId exists in the map
            if (gunItemMap.containsKey(gunId)) {
                LOGGER.info("If 2.");
                // Get the item associated with the GunId

                Item casingItem = gunItemMap.get(gunId);
                LOGGER.info("Retrieving casing item for GunId: {}. Item is {}", gunId, casingItem);
                // create new itemstack from the retrieved GunId and casing item
                ItemStack casingStack = new ItemStack(casingItem);

                // ===========================================================
                // If it does exist in the map, load the data from the config.
                // ===========================================================

                Vec3 lookDirection = gunEvent.getShooter().getLookAngle();

                //CONFIGURABLE VALUES
                // TACZEjectionConfig.EJECTION_MAP.get(); // returns same thing as below, the entire map
                //LOGGER.info("Got ejection map from ejection config: {}", TACZEjectionConfig.EJECTION_MAP.get());

                LinkedHashMap<String, TACZEjectionConfig.EjectionInfo> ejectionConfigMap = TACZEjectionConfig.EJECTION_MAP.get(); // returns same as above, the entire map

                //LOGGER.info("Got ejectionConfigMap: {}", ejectionConfigMap);

                //LOGGER.info("Attempting to separate out GunId: {}", gunId);


                //TACZEjectionConfig.EjectionInfo ejectionInfo = ejectionConfigMap.get(gunId);

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

                LOGGER.info("Deserialized EjectionInfo, based on the passed gunId: {}", ejectionInfo);
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

               double forwardOffsetDistance = 10;

                // CALCULATIONS, NOT VARIABLES, NO NEED FOR CHANGE

                //NOT A CONFIG OPTION, USED IN CALCULATIONS
                double pitchAngle = gunEvent.getShooter().getXRot();
                //DO NOT CHANGE - modify casing y position based on  player's eye height
                double offsetY = gunEvent.getShooter().getEyeHeight();
                //DO NOT CHANGE - add values of above 2
                double adjustedY = offsetY + verticalOffset;
                //DO NOT CHANGE - calculate spawn position based on isRight and sideOffsetDistance
                Vector3d sideOffset = calculateSideOffset(lookDirection, isRight, sideOffsetDistance);

                //Rotate 90 degrees from the look direction
                Vector3d offsetDirection = rotateDirection(lookDirection, rotationAngle, isRight, pitchAngle, verticalScalingFactor);
                LOGGER.info("Success obtaining velocity values from code?");
                // burst code
                if (Objects.requireNonNull(gunEvent.getShooter().getMainHandItem().getTag()).getString("GunFireMode").equals("BURST")) {
                    // determine how many burst shots there are for this gun id
                    LOGGER.info("If 3");
                    HashMap<String, Integer> gunBurstMap = parseBurstConfig();
                    if (gunBurstMap.containsKey(gunId)) {
                        LOGGER.info("If 4");
                        int burstCount = gunBurstMap.getOrDefault(gunId, 1);
                        //burst fire mode spawning two casings, main difference between this and below code and will eventually swap for handler method once I learn how to properly create one
                        LOGGER.info("Attempting burst");
                        for (int i = 0; i < burstCount; i++) {
                            //Create casing entity with velocity

                                ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX() + forwardOffsetDistance, gunEvent.getShooter().getY() + adjustedY, gunEvent.getShooter().getZ() + sideOffset.z, casingStack.copy());

                                casing.setDeltaMovement(offsetDirection.x * velocity, offsetDirection.y * velocity, offsetDirection.z * velocity);

                                casing.setPickUpDelay(20);
                                //Add casing
                                gunEvent.getShooter().level().addFreshEntity(casing);

                        }
                    }
                } else { // ie, not burst. Auto, semi, or single shot
                    LOGGER.info("Attempting shot");
                    ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY() + gunEvent.getShooter().getEyeHeight(), gunEvent.getShooter().getZ(), casingStack.copy());

                    casing.setDeltaMovement(offsetDirection.x * velocity, offsetDirection.y * velocity, offsetDirection.z * velocity);

                    casing.setPickUpDelay(20);
                    //Add casing
                    gunEvent.getShooter().level().addFreshEntity(casing);
                }
            }
            else {
            LOGGER.warn("GunID not found in gunItemMap: {}. gunItemMap: {}", gunId, gunItemMap);
            }
        }
    }

    // Vector3d helper methods for velocity code
    private static Vector3d rotateDirection(Vec3 direction, double angleDegrees, boolean isLeft, double pitchAngle, double verticalScalingFactor) {
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