package com.github.l4852.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class PlayerSpeedMixin {
    @Inject(method = "tick()V", at = @At("RETURN"))
    private void showPlayerSpeed(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();


        if (client != null && client.player != null) {

            Vec3d currentPlayerVelocity = client.player.getVelocity();

            if (client.player.hasVehicle()) {
                Entity vehicle = client.player.getVehicle();

                if (vehicle != null) {
                    currentPlayerVelocity = new Vec3d(currentPlayerVelocity.x, 0, currentPlayerVelocity.z);

                    if (vehicle instanceof MinecartEntity) {
                        Vec3d lastPos = vehicle.getLastRenderPos();
                        Vec3d currentPos = vehicle.getPos();

                        currentPlayerVelocity = lastPos.subtract(currentPos);
                    } else {
                        currentPlayerVelocity = vehicle.getVelocity();
                    }
                }
            }

            if (!(currentPlayerVelocity == null)) {
                if (Math.abs(currentPlayerVelocity.y) < 0.5) {
                    currentPlayerVelocity = new Vec3d(currentPlayerVelocity.x, 0, currentPlayerVelocity.z);
                }
                if (Math.round(currentPlayerVelocity.length() * 20 * 3.6) > 0.0) {
                    long playerSpeed = Math.round(3.6 * 20 * currentPlayerVelocity.length());

                    String speedString = playerSpeed + " km/h";

                    Text text = Text.literal(speedString);

                    client.player.sendMessage(text, true);
                }
            }
        }
    }
}