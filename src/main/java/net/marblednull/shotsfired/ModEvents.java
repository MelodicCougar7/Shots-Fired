package net.marblednull.shotsfired;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ModEvents {
    // from corrine, config parser
    public static HashMap<String, Item> parseConfig() {
        if (JsonConfig.CONFIG_MAP.isEmpty()) {
            try {
                JsonConfig.CONFIG_MAP = JsonConfig.readConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return JsonConfig.CONFIG_MAP;
    }

    public static HashMap<String, Integer> parseBurstConfig() {
        if (JsonBurstConfig.CONFIG_MAP.isEmpty()) {
            try {
                JsonBurstConfig.CONFIG_MAP = JsonBurstConfig.readConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return JsonBurstConfig.CONFIG_MAP;
    }


    //help from and credit to Leducklet/Corrineduck and ChatGPT smh

    public static void weaponShootEvent(com.tacz.guns.api.event.common.GunShootEvent gunEvent) {

        if (gunEvent.getLogicalSide().isServer()) {
            HashMap<String, Item> gunItemMap = parseConfig();

            // Get the GunId from the event
            String gunId = gunEvent.getGunItemStack().getTag().getString("GunId");

            // Check if the GunId exists in the map
            if (gunItemMap.containsKey(gunId)) {
                // Get the item associated with the GunId
                Item gunItem = gunItemMap.get(gunId);
                // create new itemstack from the retrieved GunId
                ItemStack casingStack = new ItemStack(gunItem);

                if (gunEvent.getShooter().getMainHandItem().getTag().getString("GunFireMode").equals("BURST")) {
                    HashMap<String, Integer> gunBurstMap = parseBurstConfig();
                    if (gunBurstMap.containsKey(gunId)) {
                        int burstCount = gunBurstMap.getOrDefault(gunId, 1);
                        //burst fire mode spawning two casings, main difference between this and below code and will eventually swap for handler method once I learn how to properly create one
                        for (int i = 0; i < burstCount; i++) {
                            //Create casing entity with velocity

                            Vec3 lookDirection = gunEvent.getShooter().getLookAngle();

                            //CONFIGURABLE VALUES
                                JsonEjectionConfig.EJECTION_MAP.get();

                                LinkedHashMap<String, JsonEjectionConfig.EjectionInfo> ejectionConfigMap = JsonEjectionConfig.EJECTION_MAP.get();

                                JsonEjectionConfig.EjectionInfo ejectionInfo = ejectionConfigMap.get(gunId);

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

                        }
                    }
                } else {
                   //ORIGINAL CODE, COMMENTED OUT DURING TESTING
                    //Create casing entity
                   // ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY(), gunEvent.getShooter().getZ(), casingStack.copy());
                    //casing.setPickUpDelay(20);
                    //Add casing
                    //gunEvent.getShooter().level().addFreshEntity(casing);
                    //Create casing entity
                    // now testing changes with velocity
                    Vec3 lookDirection = gunEvent.getShooter().getLookAngle();

                    //define casing velocity/speed
                    double velocity = 0.275;
                    //define whether ejection is on left or not, since all values will eventually be a config this matters little
                    boolean isLeft = true;
                    //get value of where palayer is looking for automatically adjusting casing trajectory
                    double pitchAngle = gunEvent.getShooter().getXRot();
                    //define side to side eject
                    double rotationAngle = 85.0;
                    //define arc of casing eject
                    double verticalScalingFactor = 1.0;


                    //Rotate 90 degrees from the look direction
                    Vector3d offsetDirection = rotateDirection(lookDirection, rotationAngle, isLeft, pitchAngle, verticalScalingFactor);

                    // end velocity tests
                    //original casing entity creation below
                    //ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY(), gunEvent.getShooter().getZ(), casingStack.copy());
                    // begin velocity tests
                    ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY() + gunEvent.getShooter().getEyeHeight(), gunEvent.getShooter().getZ(), casingStack.copy());

                    casing.setDeltaMovement(offsetDirection.x * velocity, offsetDirection.y * velocity, offsetDirection.z * velocity);

                    casing.setPickUpDelay(20);
                    //Add casing
                    gunEvent.getShooter().level().addFreshEntity(casing);
                }
            }
        }
    }

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