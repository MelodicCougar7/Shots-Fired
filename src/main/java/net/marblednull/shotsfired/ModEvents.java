package net.marblednull.shotsfired;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.io.IOException;
import java.util.HashMap;

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
                        //burst fire mode spawning two casings
                        for (int i = 0; i < burstCount; i++) {
                            //Create casing entity
                            // now testing changes with velocity
                            Vec3 lookDirection = gunEvent.getShooter().getLookAngle();

                            double velocity = 0.4;
                            boolean isLeft = true;
                            double rotationAngle = 45.0;

                            // Rotate 90 degrees from the look direction
                            Vector3d offsetDirection = rotateDirection(lookDirection, rotationAngle, isLeft);

                            //end velocity tests
                            //original casing entity creation
                            //ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY(), gunEvent.getShooter().getZ(), casingStack.copy());
                            // begin velocity tests
                            ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY() + gunEvent.getShooter().getEyeHeight(), gunEvent.getShooter().getZ(), casingStack.copy());

                            casing.setDeltaMovement(offsetDirection.x * velocity, offsetDirection.y * velocity, offsetDirection.z * velocity);

                            casing.setPickUpDelay(20);
                            //Add casing
                            gunEvent.getShooter().level().addFreshEntity(casing);
                        }
                    }
                } else {
                    //Create casing entity
                    ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY(), gunEvent.getShooter().getZ(), casingStack.copy());
                    casing.setPickUpDelay(20);
                    //Add casing
                    gunEvent.getShooter().level().addFreshEntity(casing);
                }
            }
        }
    }

    private static Vector3d rotateDirection(Vec3 direction, double angleDegrees, boolean isLeft) {
        // Convert angle to radians (since Java trigonometric functions use radians)
        double angleRadians = Math.toRadians(angleDegrees);

        // Rotate around the Y-axis (horizontal rotation) by the given angle
        double cosAngle = Math.cos(angleRadians);
        double sinAngle = Math.sin(angleRadians);

        double offsetX, offsetZ;

        if (isLeft) {
            // Rotate counterclockwise (left)
            offsetX = direction.x() * cosAngle - direction.z() * sinAngle;
            offsetZ = direction.x() * sinAngle + direction.z() * cosAngle;
        } else {
            // Rotate clockwise (right)
            offsetX = direction.x() * cosAngle + direction.z() * sinAngle;
            offsetZ = -direction.x() * sinAngle + direction.z() * cosAngle;
        }

        // Return the rotated direction as a Vector3d
        return new Vector3d(offsetX, direction.y(), offsetZ); // Maintain the same Y component (vertical axis)
    }
}