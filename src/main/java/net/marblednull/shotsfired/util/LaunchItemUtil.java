package net.marblednull.shotsfired.util;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// AI usage disclosure: In the interest of transparency, I inform the reader that this class was written with AI.
// The general structure, function, and purpose of this class are mine, but the exact trig calcs are not.
// I'm still learning higher level trigonometry and java syntax. If you see an obvious error, please make an issue and point it out.

public class LaunchItemUtil {

    public static void spawnAndLaunchItem(Player player, double forwardDist, double rightDist, double upDist,
                                          double yawOffsetDeg, double pitchOffsetDeg, double rollOffsetDeg,
                                          double speed, Item casingItem) {
        Level level = player.level();

        Vec3 forward = player.getLookAngle().normalize();

        float yawRad = (float) Math.toRadians(-player.getYRot());

        // Right: perpendicular to forward in the XZ plane
        Vec3 right = new Vec3(
                Math.cos(yawRad),
                0,
                -Math.sin(yawRad)
        ).normalize();

        // Up: perpendicular to forward and right
        Vec3 up = right.cross(forward).normalize();

        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 spawnPos = eyePos
                .add(forward.scale(forwardDist))
                .add(right.scale(rightDist))
                .add(up.scale(upDist));

        Vec3 launchDir = rotateDirection(forward, right, up,
                yawOffsetDeg, pitchOffsetDeg, rollOffsetDeg);

        ItemStack casingStack = new ItemStack(casingItem);
        ItemEntity casingEntity = new ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, casingStack);

        casingEntity.setDeltaMovement(launchDir.scale(speed));
        casingEntity.setDefaultPickUpDelay();
        player.level().addFreshEntity(casingEntity);
    }

    // Rotate direction vector using yaw/pitch/roll offsets
    private static Vec3 rotateDirection(Vec3 forward, Vec3 right, Vec3 up,
                                        double yawDeg, double pitchDeg, double rollDeg) {
        double yaw   = Math.toRadians(yawDeg);   // rotate around Up
        double pitch = Math.toRadians(pitchDeg); // rotate around Right
        double roll  = Math.toRadians(rollDeg);  // rotate around Forward

        Vec3 dir = forward;
        dir = rotateAroundAxis(dir, up, yaw);
        dir = rotateAroundAxis(dir, right, pitch);
        dir = rotateAroundAxis(dir, forward, roll);

        return dir.normalize();
    }

    // Axis-angle rotation via Rodrigues' formula
    private static Vec3 rotateAroundAxis(Vec3 vec, Vec3 axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return vec.scale(cos)
                .add(axis.cross(vec).scale(sin))
                .add(axis.scale(axis.dot(vec) * (1 - cos)));
    }
}

/// Example usage
/*

// Launch a diamond 2 blocks forward, 0.5 up, yawed 20° right and pitched 10° up, with speed 1.5
LaunchItemUtil.spawnAndLaunchItem(player,
    2.0, 0.0, 0.5,    // position offsets (forward, right, up)
    20.0, 10.0, 0.0,  // yaw, pitch, roll
    1.5               // speed
);




 */