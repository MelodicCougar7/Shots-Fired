package net.marblednull.shotsfired;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.util.HashMap;

public class ModEvents {
    // from corrine, config parser
    public static HashMap<String, Item> parseConfig() {
        if(JsonConfig.CONFIG_MAP.isEmpty()) {
            try {
                JsonConfig.CONFIG_MAP = JsonConfig.readConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return JsonConfig.CONFIG_MAP;
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
                    HashMap<String, Integer> gunBurstMap = JsonBurstConfig.CONFIG_MAP;
                    if (gunBurstMap.containsKey(gunId)) {
                        int burstCount = gunBurstMap.getOrDefault(gunId, 1);
                        //burst fire mode spawning two casings
                        for (int i = 0; i < burstCount; i++) {
                            //Create casing entity
                            ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY(), gunEvent.getShooter().getZ(), casingStack.copy());
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
}