package net.marblednull.shotsfired;

import com.mojang.logging.LogUtils;
import net.marblednull.shotsfired.config.EntityBlacklist;
import net.marblednull.shotsfired.config.TACZConfig;
import net.marblednull.shotsfired.config.TACZEjectionConfig;
import net.marblednull.shotsfired.util.DropData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Map;

import static net.marblednull.shotsfired.util.LaunchItemUtil.spawnAndLaunchItem;

public class ModEvents {
    ///  Main class that handles the casing firing sequence and casing creation, while item physics are gotten from a util.
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final RandomSource randomBulletChance = RandomSource.create();

    //help from and credit to Leducklet/Corrineduck and ChatGPT smh

    public static void spawnCasing(com.tacz.guns.api.event.common.GunFireEvent gunEvent, Item casingItem, double dropChance, String gunId) {
        //Create casing entity

        // Allow casing creation if this is true.
        // dropChance is thus the chance to spawn a casing.
        // 100 > 99.9, so continue
        if (dropChance >= randomBulletChance.nextFloat() * 100) {
            //LOGGER.warn("Casing broke! Ignoring further shot creation");

            //Create casing entity with velocity

            Map<String, TACZEjectionConfig.EjectionInfo> ejectionConfigMap = TACZEjectionConfig.EJECTION_MAP.get();
            TACZEjectionConfig.EjectionInfo ejectionInfoByGun;
            if (ejectionConfigMap.containsKey(gunId)) {
                ejectionInfoByGun = ejectionConfigMap.get(gunId);
            } else {
                ejectionInfoByGun = ejectionConfigMap.get("fallback");
            }

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
        }
    }

    public static void weaponShootEvent(com.tacz.guns.api.event.common.GunFireEvent gunEvent) {
        if (gunEvent.getLogicalSide().isServer()) {
            // return early if the shooter is in the blacklist
            for (String entityIdString : EntityBlacklist.BLACKLIST.get()) {
                if (gunEvent.getShooter().getType() == ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityIdString))) {
                    return;
                }
            }

            // LOGGER.warn("weaponShootEvent called");
            Map<String, DropData> gunItemMap = TACZConfig.TACZ.get();
            // Get the GunId from the event
            String gunId = gunEvent.getGunItemStack().getTag().getString("GunId");
            // Check if the GunId exists in the map
            if (gunItemMap.containsKey(gunId)) {
                // Get the item associated with the GunId
                Item casingItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(gunItemMap.get(gunId).item));
                // create new itemstack from the retrieved GunId
                // The chance the item will drop from the gun
                float dropChance = gunItemMap.get(gunId).chance;
                spawnCasing(gunEvent, casingItem, dropChance, gunId);

            }
            // end of gunItemMap,gunId check, and casing spawning
        }
    }
}
