package net.marblednull.shotsfired.mixin;

import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static net.marblednull.shotsfired.ModEvents.rotateDirection;


@Mixin(
        value = {GunItem.class},
        remap = false,
        priority = 1300
)
public abstract class CasingMixin {
    @Inject(
            method = {"handleClientHitScanFireRequest"},
            at = {@At("RETURN")},
            cancellable = true
    )
    public void onHandleClientHitScanFireRequest(ServerPlayer player, FireModeInstance fireModeInstance, UUID stateId, int slotIndex, int correlationId, boolean isAiming, long requestSeed, CallbackInfo ci) {
        Vec3 lookDirection = player.getViewVector(0.0F);
        ItemStack casingStack = player.getInventory().getItem(slotIndex);

        //define casing velocity/speed
        double Casingvelocity = 0.275;
        //define whether ejection is on left or not, since all values will eventually be a config this matters little
        boolean CasingisLeft = true;
        //get value of where palayer is looking for automatically adjusting casing trajectory
        double CasingpitchAngle = player.getXRot();
        //define side to side eject
        double CasingrotationAngle = 85.0;
        //define arc of casing eject
        double CasingverticalScalingFactor = 1.0;


        //Rotate 90 degrees from the look direction

        Vector3d offsetDirection = rotateDirection(lookDirection, CasingrotationAngle, CasingisLeft, CasingpitchAngle, CasingverticalScalingFactor);

        // end velocity tests
        //original casing entity creation below
        //ItemEntity casing = new ItemEntity(gunEvent.getShooter().level(), gunEvent.getShooter().getX(), gunEvent.getShooter().getY(), gunEvent.getShooter().getZ(), casingStack.copy());
        // begin velocity tests
        ItemEntity casing = new ItemEntity(player.level(), player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), casingStack.copy());

        casing.setDeltaMovement(offsetDirection.x * Casingvelocity, offsetDirection.y * Casingvelocity, offsetDirection.z * Casingvelocity);

        casing.setPickUpDelay(20);
        //Add casing
        player.level().addFreshEntity(casing);

    }
}
