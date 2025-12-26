package com.github.l4852.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatEntity.class)
public abstract class BoatSpeedMixin {
    @Shadow
    private AbstractBoatEntity.Location location;

    @Shadow
    private boolean pressingForward;

    @Shadow
    private boolean pressingBack;

    @Shadow
    private boolean pressingLeft;

    @Shadow
    private float yawVelocity;

    @Shadow
    private boolean pressingRight;

    @Inject(method = "updateVelocity()V", at = @At("RETURN"))
    private void editBoatDrag(CallbackInfo ci) {
        AbstractBoatEntity boat = (AbstractBoatEntity) (Object) this;
        Vec3d currentVelocity = boat.getVelocity();

        float vanillaDragFactor = 0.9F;
        double customDragFactor = 0.965;

        // Modify in-water velocity cap and drag deceleration
        if (this.location == AbstractBoatEntity.Location.IN_WATER) {
            // Cancel out vanilla drag and apply custom drag
            boat.setVelocity(currentVelocity.x / vanillaDragFactor * customDragFactor, currentVelocity.y, currentVelocity.z / vanillaDragFactor * customDragFactor);
        }
    }

    @Inject(method = "updatePaddles()V", at = @At("RETURN"))
    private void editBoatVelocity(CallbackInfo ci) {
        AbstractBoatEntity boat = (AbstractBoatEntity) (Object) this;
        Vec3d currentVelocity3D = boat.getVelocity();
        Vec3d currentVelocity = new Vec3d(currentVelocity3D.x, 0, currentVelocity3D.z);

        // Max velocity in km/h
        double MAX_VELOCITY = 80.0;
        // In blocks / tick
        double maxVelocityScaled = MAX_VELOCITY / 3.6 / 20;

        float inputFactor = 0.0F;

        if (boat.hasPassengers()) {
            if (this.pressingForward) {
                inputFactor = 0.015F;
            }
            if (this.pressingBack) {
                inputFactor = -0.01F;
            }

            double currentSpeed = currentVelocity.length() * 20 * 3.6;

            if (Math.round(currentSpeed) <= maxVelocityScaled) {
                boat.setVelocity(currentVelocity.add((double) (MathHelper.sin(-boat.getYaw() * 0.017453292F) * inputFactor), 0.0, (double) (MathHelper.cos(boat.getYaw() * 0.017453292F) * inputFactor)));
                boat.setPaddlesMoving(this.pressingRight && !this.pressingLeft || this.pressingForward, this.pressingLeft && !this.pressingRight || this.pressingForward);
            } else {
                currentVelocity = new Vec3d(currentVelocity3D.x, 0, currentVelocity3D.z);
            }

        }
    }
}