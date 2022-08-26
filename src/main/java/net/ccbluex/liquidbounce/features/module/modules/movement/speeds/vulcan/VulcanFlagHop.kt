package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook


class VulcanFlagHop : SpeedMode("VulcanFlagHop") {
    var offGroundTicks = 0
    var onGroundTicks = 0
    override fun onPreMotion() {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0
            onGroundTicks += 1
        } else {
            offGroundTicks += 1
            onGroundTicks = 0
        }
        if (mc.thePlayer.onGround && mc.thePlayer.motionY > -.2) {
            mc.netHandler.networkManager.sendPacket(C04PacketPlayerPosition((mc.thePlayer.posX + mc.thePlayer.lastTickPosX) / 2, (mc.thePlayer.posY + mc.thePlayer.lastTickPosY) / 2 - 0.0784000015258789, (mc.thePlayer.posZ + mc.thePlayer.lastTickPosZ) / 2, false))
            mc.netHandler.networkManager.sendPacket(C06PacketPlayerPosLook((mc.thePlayer.posX + mc.thePlayer.lastTickPosX) / 2, (mc.thePlayer.posY + mc.thePlayer.lastTickPosY) / 2, (mc.thePlayer.posZ + mc.thePlayer.lastTickPosZ) / 2, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
            mc.netHandler.networkManager.sendPacket(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
            mc.netHandler.networkManager.sendPacket(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784000015258789, mc.thePlayer.posZ, false))
            mc.netHandler.networkManager.sendPacket(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
            MovementUtils.strafe(MovementUtils.getBaseMoveSpeed() * 1.25 * 2)
        } else if (offGroundTicks == 1) {
            MovementUtils.strafe(MovementUtils.getBaseMoveSpeed() * 0.91f)
        }
    }

    override fun onPacket(event: PacketEvent) {
        val p = event.packet
        if (p is S08PacketPlayerPosLook && mc.thePlayer.ticksExisted > 20) {
            if (mc.thePlayer.getDistanceSq(p.x, p.y, p.z) < 25 * 4) {
                event.cancelEvent()
            }
        }
    }
}
