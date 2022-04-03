package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.sqrt


class VerusBhop : SpeedMode("VerusBhop") {
    var tick = 0
    private var jumpY = 0.0
    private var bypassTicks = 0

    override fun onMove(event: MoveEvent) {
        var speedBoost = 0.0;
        --bypassTicks;
        ++tick;
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            MovementUtils.strafe(0.35);
            mc.thePlayer.jump();
            jumpY = mc.thePlayer.posY;
        }
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.fallDistance >= 1.5) {
                if (bypassTicks > 0) {
                    MovementUtils.strafe(1.0F);
                }
                else {
                    MovementUtils.strafe(0.26);
                }
            }
            else if (bypassTicks > 0) {
                MovementUtils.strafe(1.0 + speedBoost);
            }
            else {
                MovementUtils.strafe(0.33 + speedBoost);
                if (mc.thePlayer.posY - jumpY < 0.35) {
                    MovementUtils.strafe(0.5 + speedBoost);
                }
            }
        }
        else {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionZ = 0.0;
        }
    }
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            bypassTicks = 20
        }
    }
}