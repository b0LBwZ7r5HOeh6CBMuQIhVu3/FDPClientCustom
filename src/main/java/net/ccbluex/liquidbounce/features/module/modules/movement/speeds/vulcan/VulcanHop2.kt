package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PlayerUtil
import net.minecraft.block.BlockAir
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos


class VulcanHop2 : SpeedMode("VulcanHop2") {
    var offGroundTicks = 0
    var bool = false
    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                val speed: Double = MovementUtils.getBaseMoveSpeed() - 0.01
                MovementUtils.strafe(speed - Math.random() / 2000)
                mc.thePlayer.jump()
                bool = true
            } else {
                if (bool) {
                    if (offGroundTicks > 3) mc.thePlayer.motionY = (mc.thePlayer.motionY - 0.08) * 0.98F
                    if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX + 0.0, mc.thePlayer.posY + 2.0, mc.thePlayer.posZ + 0.0)).getBlock() !is BlockAir) MovementUtils.strafe(MovementUtils.getSpeed() * (1.1 - Math.random() / 500))

                }
                if (mc.thePlayer.isInWater() || mc.thePlayer.hurtTime == 9) MovementUtils.strafe()
            }
        } else MovementUtils.strafe(0f)
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
